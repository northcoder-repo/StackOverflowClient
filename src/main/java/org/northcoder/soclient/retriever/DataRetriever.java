package org.northcoder.soclient.retriever;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.json.JsonWriterSettings;
import org.northcoder.soclient.QuestionsDB;
import org.northcoder.soclient.records.Question;

// https://www.mongodb.com/docs/drivers/java/sync/current/usage-examples
class DataRetriever {

    final static ObjectMapper mapper = new ObjectMapper();

    private final QuestionsDB qdb;

    DataRetriever(QuestionsDB qdb) {
        this.qdb = qdb;
    }

    protected JsonNode getAllQuestions() throws JsonProcessingException {
        // extract subset from all docs:
        JsonWriterSettings writer = JsonWriterSettings.builder().indent(true).build();

        Bson projectionFields = Projections.fields(
                Projections.include("question_id", "title", "tags", "site"),
                Projections.excludeId());

        StringBuilder sb = new StringBuilder();

        try (MongoCursor<Document> cursor = qdb.collection().find()
                .projection(projectionFields)
                .sort(Sorts.ascending("title"))
                .iterator()) {
            while (cursor.hasNext()) {
                sb.append(cursor.next().toJson(writer)).append(", ");
            }
        }
        String innerJson = sb.substring(0, sb.length() - 2); // remove final ", "
        return mapper.readTree(String.format("{ \"data\": [ %s ] }", innerJson));
    }

    protected Question getQuestionDetails(String site, int questionId) throws JsonProcessingException {
        JsonWriterSettings writer = JsonWriterSettings.builder().indent(true).build();

        Bson filter = Filters.and(
                Filters.eq("question_id", questionId),
                Filters.eq("site", site));
        Bson projectionFields = Projections.fields(
                Projections.excludeId());
        Document doc = qdb.collection().find(filter)
                .projection(projectionFields)
                .first();

        String jsonString = (doc != null) ? doc.toJson(writer) : "{}";
        return mapper.readValue(jsonString, Question.class);
    }

}
