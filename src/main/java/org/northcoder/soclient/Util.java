package org.northcoder.soclient;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import org.bson.Document;
import org.bson.RawBsonDocument;
import org.bson.conversions.Bson;
import org.eclipse.jetty.http.HttpCookie;
import org.eclipse.jetty.server.session.SessionHandler;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

public class Util {

    public static final int MAX_IDS = 30;
    public static final String OK = "OK";
    public static final String OK_FINISHED = "<span class=\"ok_msg\">Finished!</span>";
    public static final String ERR_PREFIX = "<span class=\"error_msg\">ERROR</span>";
    public static final String ERR_NO_SITE = ERR_PREFIX + " - no Stack Exchange site selected.";
    public static final String ERR_INVALID_SITE = ERR_PREFIX + " - invalid Stack Exchange site.";
    public static final String ERR_NO_IDS = ERR_PREFIX + " - no question IDs entered.";
    public static final String ERR_TOO_MANY_IDS = ERR_PREFIX + " - more than " + MAX_IDS + " question IDs entered.";
    public static final String ERR_NO_QUESTIONS = ERR_PREFIX + " No questions found for question ID list.";
    public static final String ERR_TOO_MANY_REQS = ERR_PREFIX + " Too many requests from this IP.";
    public static final String ERR_TRY_AGAIN_LATER = ERR_PREFIX + " Try again in %s.";
    public static final String INFO_QU_ALREADY_LOADED = "%s - %s [<span class=\"warning_msg\">already loaded</span>]";

    public static String formatDateTime(long epochSeconds) {
        DateTimeFormatter dtf = DateTimeFormatter
                .ofPattern("d MMM yyyy 'at' HH:mm");
        ZonedDateTime zdt = Instant
                .ofEpochSecond(epochSeconds)
                .atZone(ZoneId.of("UTC"));
        return dtf.format(zdt);
    }

    protected static TemplateEngine configureThymeleaf() {
        ClassLoaderTemplateResolver templateResolver
                = new ClassLoaderTemplateResolver(Thread
                        .currentThread().getContextClassLoader());
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCharacterEncoding("UTF-8");
        templateResolver.setPrefix("/thymeleaf/");
        templateResolver.setSuffix(".html");
        templateResolver.setCacheTTLMs(3600000L); // one hour
        templateResolver.setCacheable(true);
        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);
        return templateEngine;
    }

    protected static SessionHandler soSessionHandler() {
        SessionHandler sessionHandler = new SessionHandler();
        sessionHandler.setSameSite(HttpCookie.SameSite.STRICT);
        sessionHandler.setHttpOnly(true);
        return sessionHandler;
    }

    //
    // MongoDB utils:
    //
    // used for a one-off update to the collection:
    protected static void updateDocs(QuestionsDB qdb) {

        try (MongoCursor<Document> cursor = qdb.collection().find().iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                Integer question_id = doc.get("question_id", Integer.class);
                String link = doc.get("link", String.class);
                String site = link.substring(8, link.indexOf('.'));
                Document query = new Document().append("question_id", question_id);
                Bson updates = Updates.combine(Updates.set("site", site));
                UpdateOptions options = new UpdateOptions().upsert(false);
                UpdateResult result = qdb.collection().updateOne(query, updates, options);
                System.out.println("modified " + result.getModifiedCount());
            }
        }
    }

    // Max supported BSON doc size is 16 MB.
    protected static void getMaxDocSize(QuestionsDB qdb) {
        MongoClient mongoClient = MongoClients.create(qdb.uri());
        MongoDatabase db = mongoClient.getDatabase(qdb.dbName());
        MongoCollection<RawBsonDocument> mongoCollection = db
                .getCollection(qdb.collectionName(), RawBsonDocument.class);

        MongoCursor<RawBsonDocument> cursor = mongoCollection.find().iterator();

        int maxSize = 0;
        String title = "";

        while (cursor.hasNext()) {
            RawBsonDocument doc = cursor.next();
            String s = doc.get("title").asString().getValue();
            int size = doc.getByteBuffer().remaining(); // bytes
            if (size > maxSize) {
                maxSize = size;
                title = s;
            }
        }
        System.out.println("Max. size is " + (maxSize / 1024.0) + " KB for " + title);
    }

}
