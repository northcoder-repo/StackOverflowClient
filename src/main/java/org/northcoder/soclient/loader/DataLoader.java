package org.northcoder.soclient.loader;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoException;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.InsertManyResult;
import io.javalin.websocket.WsMessageContext;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.northcoder.soclient.QuestionsDB;
import org.northcoder.soclient.Util;
import org.northcoder.soclient.records.Answer;
import org.northcoder.soclient.records.Comment;
import org.northcoder.soclient.records.Comments;
import org.northcoder.soclient.records.Item;
import org.northcoder.soclient.records.Question;
import org.northcoder.soclient.records.QuestionLoadList;
import org.northcoder.soclient.records.Questions;

class DataLoader {

    private static final int MAX_QUOTA = 300;
    private static final String BASE_URL = "https://api.stackexchange.com/2.3/%s/%s?%s%s";
    private static final String QUERY_PARAMS = "site=%s&include=withbody&unsafe=false&filter=";

    private final WsMessageContext wsCtx;
    private final QuestionsDB questionsDB;

    DataLoader(WsMessageContext wsCtx, QuestionsDB questionsDB) {
        this.wsCtx = wsCtx;
        this.questionsDB = questionsDB;
    }

    protected void loadQuestionData(QuestionLoadList qLL)
            throws SQLException, URISyntaxException,
            IOException, InterruptedException {

        // the API uses a semicolon-separated list:
        String questionIds = dedupe(qLL.question_site(), qLL.question_ids());

        Questions questions = getQuestions(qLL.question_site(), questionIds);
        if (questions != null && !questions.items().isEmpty()) {
            saveToDb(questions);
        } else {
            sendSocketMessage(Util.ERR_NO_QUESTIONS);
        }
    }

    private Questions getQuestions(String site, String questionIds) throws URISyntaxException,
            IOException, InterruptedException {

        // could be empty after de-duplication of provided list and removal of IDs
        // which are already in the DB (and which are not reloaded):
        if (questionIds.isEmpty()) {
            return null;
        }

        final String topic = "questions";
        // filter builder: https://api.stackexchange.com/docs/create-filter
        // gets questions and answers and comment links:
        final String filterId = "!22ZfkjAc8nKg12AFnY6GD";
        //
        // example api url:
        // https://api.stackexchange.com/2.3/questions/406230;40480?site=stackoverflow&include=withbody&unsafe=false&filter=!22ZfkjAc8nKg12AFnY6GD
        //
        final String queryParams = String.format(QUERY_PARAMS, site);
        final String url = String.format(BASE_URL, topic, questionIds, queryParams, filterId);
        int quotaRemaining;
        final String data = fetchData(url);

        if (data == null) {
            return null;
        }

        final ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        final Questions questions = mapper.readValue(data, Questions.class);
        quotaRemaining = questions.quota_remaining();

        final Comparator answersOrder = Comparator
                .comparing(Answer::score).reversed()
                .thenComparing(Answer::creation_date);

        System.out.println("");

        for (Question question : questions.items()) {
            if (question.answers() != null) {
                Collections.sort(question.answers(), answersOrder);
            }
            Integer i = fetchCommentBodies(site, question, mapper);
            quotaRemaining = (i == null) ? quotaRemaining : i;

            String msg = question.question_id() + " - " + question.title();
            System.out.println("Processing question: " + msg);
            sendSocketMessage(msg);

            if (question.answers() != null) {
                for (Answer answer : question.answers()) {
                    Integer j = fetchCommentBodies(site, answer, mapper);
                    quotaRemaining = (j == null) ? quotaRemaining : j;
                }
            }
        }
        System.out.println("\nQuota remaining: " + quotaRemaining + " of " + MAX_QUOTA + "\n");
        sendSocketMessage("Quota remaining (approx.): " + quotaRemaining);
        return questions;
    }

    private String fetchData(String url) throws URISyntaxException, IOException, InterruptedException {
        final URI uri = new URI(url);
        final HttpClient client = HttpClient.newBuilder().build();
        final HttpRequest request = HttpRequest.newBuilder().uri(uri).build();

        System.out.println("Sending URL: " + url);
        final HttpResponse<InputStream> response = client
                .send(request, HttpResponse.BodyHandlers.ofInputStream());
        // the response is gzipped JSON - need to explicitly unzip it:
        final BufferedReader br = new BufferedReader(new InputStreamReader(
                new GZIPInputStream(response.body()), StandardCharsets.UTF_8));

        String line;
        final StringBuilder sb = new StringBuilder();
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        String data = sb.toString();

        if (quotaExceeded(data)) {
            return null;
        }
        return data;
    }

    // the questions API call does not want to fetch comment bodies - just the
    // comment metadata, so we have to fetch comments separately.
    private Integer fetchCommentBodies(String site, Item item, ObjectMapper mapper)
            throws URISyntaxException, IOException, InterruptedException {
        if (item.comments() == null || item.comments().isEmpty()) {
            return null;
        }
        final Comparator commentsOrder = Comparator.comparing(Comment::creation_date);
        StringBuilder sb = new StringBuilder();

        // We only fetch the first 30 comments per Q or A, so we don't make
        // multiple calls from our limited API quota:
        if (item.comments() != null) {
            Collections.sort(item.comments(), commentsOrder);
        }
        int commentsCount = Math.min(30, item.comments().size());
        item.comments().subList(0, commentsCount).forEach(comment -> {
            sb.append(comment.comment_id()).append(";");
        });
        String commentIds = sb.substring(0, sb.length() - 1);
        final String topic = "comments";
        final String filterId = "!nNPvSN_LEO"; // comment body filter
        final String queryParams = String.format(QUERY_PARAMS, site);
        final String url = String.format(BASE_URL, topic, commentIds, queryParams, filterId);
        final String data = fetchData(url);
        if (data == null) {
            return 0;
        }
        final Comments comments = mapper.readValue(data, Comments.class);
        if (comments.items() != null) {
            Collections.sort(comments.items(), commentsOrder);
        }
        // replace comments which have no comment bodies with ones which do have bodies:
        item.comments().clear();
        if (comments.items() != null) {
            comments.items().forEach(comment -> {
                item.comments().add(comment);
            });
        }
        return comments.quota_remaining();
    }

    // https://www.mongodb.com/docs/drivers/java/sync/current/usage-examples
    private void saveToDb(Questions questions) throws SQLException, JsonProcessingException {

        // convert question pojos to list of bson documents:
        final ObjectMapper mapper = new ObjectMapper();
        List<Document> questionsList = new ArrayList<>();
        for (Question question : questions.items()) {
            Document doc = Document.parse(mapper.writeValueAsString(question));
            questionsList.add(doc);
        }

        // insert bson docs to mongodb:
        try {
            InsertManyResult result = questionsDB.collection().insertMany(questionsList);
            System.out.println("\nInserted document ids: " + result.getInsertedIds() + "\n");
        } catch (MongoException me) {
            System.err.println("\nUnable to insert due to an error: " + me + "\n");
        }
    }

    private String dedupe(String site, List<Integer> questionList) {
        StringBuilder sb = new StringBuilder();
        for (int question_id : questionList) {
            if (!questionExists(site, question_id)) {
                sb.append(question_id).append(";");
            }
        }
        return sb.substring(0, Math.max(0, sb.length() - 1));
    }

    private boolean questionExists(String site, int question_id) {
        Bson filter = Filters.and(
                Filters.eq("question_id", question_id),
                Filters.eq("site", site));
        Document doc = questionsDB.collection().find(filter).first();

        if (doc != null) {
            String title = doc.get("title", String.class);
            sendSocketMessage(String.format(Util.INFO_QU_ALREADY_LOADED,
                    question_id, title));
        }
        return doc != null;
    }

    private boolean quotaExceeded(String data) {
        // sample error:
        // {"error_id":502,"error_message":"too many requests from this IP, more requests available in 84469 seconds","error_name":"throttle_violation"}
        if (data.contains("throttle_violation")) {
            Integer totalSecs;
            String timeString = null;
            Pattern pattern = Pattern.compile("(available in )(\\d+)( seconds)");
            Matcher matcher = pattern.matcher(data);
            if (matcher.find()) {
                totalSecs = Integer.valueOf(matcher.group(2));
                int hours = totalSecs / 3600;
                int minutes = (totalSecs % 3600) / 60;
                timeString = String.format("%02d hours %02d minutes", hours, minutes);
            }
            sendSocketMessage(Util.ERR_TOO_MANY_REQS);
            if (timeString != null) {
                sendSocketMessage(String.format(Util.ERR_TRY_AGAIN_LATER, timeString));
            }
            return true;
        } else {
            return false;
        }
    }

    private void sendSocketMessage(String msg) {
        String ts = ZonedDateTime
                .now(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        wsCtx.send("[" + ts + "] " + msg);
    }

}
