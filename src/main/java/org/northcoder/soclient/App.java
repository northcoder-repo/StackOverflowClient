package org.northcoder.soclient;

import io.javalin.Javalin;
import static io.javalin.apibuilder.ApiBuilder.*;
import io.javalin.config.JavalinConfig;
import io.javalin.http.staticfiles.Location;
import io.javalin.rendering.template.JavalinThymeleaf;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import org.northcoder.soclient.loader.LoaderController;
import org.northcoder.soclient.retriever.RetrieverController;

public class App {

    private final String uri = "mongodb://localhost:27017";
    private final String dbName = "stackoverflow";
    private final String collectionName = "questions";

    public static void main(String[] args) throws SQLException, URISyntaxException,
            IOException, InterruptedException {
        App app = new App();
        //Util.getMaxDocSize();
        Javalin javalin = Javalin.create(config -> {
            app.configureJavalin(config);
        }).start(8091);
        app.handleRoutes(javalin);
    }

    private void configureJavalin(JavalinConfig config) {
        config.jetty.sessionHandler(() -> Util.soSessionHandler());
        JavalinThymeleaf.init(Util.configureThymeleaf());
        config.showJavalinBanner = true;
        config.staticFiles.add("/public", Location.CLASSPATH);
    }

    private void handleRoutes(Javalin javalin) {
        QuestionsDB qdb = new QuestionsDB(uri, dbName, collectionName);
        LoaderController loaderCtl = new LoaderController(qdb);
        RetrieverController retrieverCtl = new RetrieverController(qdb);
        javalin.routes(() -> {
            // HTML:
            get("/questions", retrieverCtl::fetchQuestions);
            get("/question/{site}/{question_id}", retrieverCtl::fetchOneQuestion);
            get("/load_data", loaderCtl::loadDataList);
            // JSON:
            get("/questions_list", retrieverCtl::fetchQuestionsJson);
        });
        // data loader websocket:
        javalin.ws("/websocket/do_load", (ws) -> {
            ws.onMessage(loaderCtl::websocketLoadData);
        });
    }

}
