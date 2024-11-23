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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class StaticSiteController {

    //
    // run the static site using the following from command line in /static_site:
    //
    // python -m http.server
    //
    // this serves the homepage at localhost:8000
    //
    public void build(Context ctx) throws MalformedURLException, IOException {
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

    private void writeFile(String site, String questionId) throws FileNotFoundException, IOException {
        String path = "question/" + site + "/" + questionId;
        File page = new File("static_site/" + path + "/index.html");
        String pageUrl = "http://localhost:8091/" + path;
        page.getParentFile().mkdirs();
        try (PrintStream out = new PrintStream(new FileOutputStream(page))) {
            Document doc = Jsoup.connect(pageUrl).get();
            out.print(doc.toString());
        }
    }

}
