package org.northcoder.soclient.staticsite;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.http.Context;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class StaticSiteController {

    //
    // run the static site using the following from command line in /static_site:
    //
    // python -m http.server
    //
    // this serves the homepage at localhost:8000
    //
    public void build(Context ctx) throws MalformedURLException, IOException, Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode questions = mapper
                .readTree(URI.create("http://localhost:8091/questions_list").toURL())
                .get("data");
        for (var question : questions) {
            String site = question.get("site").textValue();
            String questionId = question.get("question_id").asText();
            writeFile(site, questionId);
        }
        ctx.result("Done.");
    }

    private void writeFile(String site, String questionId) throws FileNotFoundException, IOException, Exception {
        String path = "question/" + site + "/" + questionId;
        File pageFile = new File("static_site/" + path + "/index.html");
        String pageUrl = "http://localhost:8091/" + path;
        pageFile.getParentFile().mkdirs();

        Document doc = Jsoup.connect(pageUrl).get();

        getImages(site, questionId, doc);
        try (PrintStream out = new PrintStream(new FileOutputStream(pageFile))) {
            out.print(doc.toString());
        }
    }

    private void getImages(String site, String questionId, Document doc) throws Exception {
        Elements imgurs = doc.select("img[src*=imgur.com]");

        for (Element imgur : imgurs) {
            try {
                String imageFileName = getImage(site, questionId, imgur);
                // replace external URL with local URL:
                imgur.attr("src", imageFileName);
            } catch (IOException ex) {
                throw ex;
            }

        }
    }

    private String getImage(String site, String questionId, Element imgur)
            throws MalformedURLException, IOException {
        URL url = URI.create(imgur.attr("src")).toURL();
        String[] segments = url.getPath().split("/");
        String fileName = segments[segments.length - 1];
        ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
        String filePath = "static_site/question/" + site + "/" + questionId + "/" + fileName;
        System.out.println("Processing image: " + filePath);
        FileOutputStream fileOutputStream = new FileOutputStream(filePath);
        FileChannel fileChannel = fileOutputStream.getChannel();
        fileChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
        return fileName;
    }

}
