package org.northcoder.soclient.loader;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mongodb.client.model.CountOptions;
import static com.mongodb.client.model.Filters.gt;
import io.javalin.http.Context;
import io.javalin.websocket.WsMessageContext;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bson.BsonDocument;
import org.bson.conversions.Bson;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.northcoder.soclient.QuestionsDB;
import org.northcoder.soclient.Site;
import org.northcoder.soclient.Util;
import org.northcoder.soclient.records.Question;
import org.northcoder.soclient.records.QuestionLoadList;
import org.northcoder.soclient.retriever.RetrieverController;

public class ControllerTests {

    private final Context mockCtx = mock(Context.class);

    // DB-related tests are integration tests using an actual DB instance:
    private final String uri = "mongodb://localhost:27017";
    private final String dbName = "test";
    private final String collectionName = "test_questions";
    private final QuestionsDB qdb = new QuestionsDB(uri, dbName, collectionName);
    RetrieverController retrieverCtl = new RetrieverController(qdb);

    //@Test
    public void testLoadDataList() {
        new LoaderController(qdb).loadDataList(mockCtx);
        Map<String, Object> model = new HashMap<>();
        model.put("sites", Site.class);
        verify(mockCtx).render("load_data.html", model);
    }

    @Test
    public void testLoaderNullSite() throws SQLException, URISyntaxException,
            IOException, InterruptedException {
        // inputs:
        String question_site = null;
        List<Integer> question_ids = new ArrayList<>();
        // expected output:
        String message = Util.ERR_NO_SITE;
        // call method:
        testWebsocketLoadData(question_site, question_ids, message);
    }

    @Test
    public void testLoaderEmptySite() throws SQLException, URISyntaxException,
            IOException, InterruptedException {
        // inputs:
        String question_site = " ";
        List<Integer> question_ids = new ArrayList<>();
        // expected output:
        String message = Util.ERR_NO_SITE;
        // call method:
        testWebsocketLoadData(question_site, question_ids, message);
    }

    @Test
    public void testLoaderInvalidSite() throws SQLException, URISyntaxException,
            IOException, InterruptedException {
        // inputs:
        String question_site = "nosuchsite";
        List<Integer> question_ids = new ArrayList<>();
        // expected output:
        String message = Util.ERR_INVALID_SITE;
        // call method:
        testWebsocketLoadData(question_site, question_ids, message);
    }

    @Test
    public void testLoaderEmptyIdList() throws SQLException, URISyntaxException,
            IOException, InterruptedException {
        // inputs:
        String question_site = "stackoverflow";
        List<Integer> question_ids = new ArrayList<>();
        // expected output:
        String message = Util.ERR_NO_IDS;
        // call method:
        testWebsocketLoadData(question_site, question_ids, message);
    }

    @Test
    public void testLoaderInvalidIdList_null() throws SQLException, URISyntaxException,
            IOException, InterruptedException {
        // inputs:
        String question_site = "stackoverflow";
        List<Integer> question_ids = new ArrayList<>();
        question_ids.add(null);
        // expected output:
        String message = Util.ERR_NO_IDS;
        // call method:
        testWebsocketLoadData(question_site, question_ids, message);
    }

    @Test
    public void testLoaderInvalidIdList_zero() throws SQLException, URISyntaxException,
            IOException, InterruptedException {
        // inputs:
        String question_site = "stackoverflow";
        List<Integer> question_ids = new ArrayList<>();
        question_ids.add(0);
        // expected output:
        String message = Util.ERR_NO_IDS;
        // call method:
        testWebsocketLoadData(question_site, question_ids, message);
    }

    private static final int TEST_QU_ID = 1247627;
    private static final String TEST_QU_TITLE = "Java Component based vs Request based frameworks";
    private static final List<String> TEST_TAGS_LIST = Arrays
            .asList("java", "frameworks", "struts", "ejb", "components");

    @Test
    public void testLoaderValidIdList_one() throws SQLException, URISyntaxException,
            IOException, InterruptedException {
        deleteDocuments();
        assertTrue(countDocuments() == 0);
        // inputs:
        String question_site = "stackoverflow";
        List<Integer> question_ids = new ArrayList<>();

        question_ids.add(TEST_QU_ID);
        // expected output:
        String message = Util.OK_FINISHED;
        // call method:
        testWebsocketLoadData(question_site, question_ids, message);
        assertTrue(countDocuments() == 1);
    }

    @Test
    public void testLoaderValidIdList_duplicate() throws SQLException, URISyntaxException,
            IOException, InterruptedException {
        assertTrue(countDocuments() == 1);
        // reload the same doc:
        testLoaderValidIdList_one();
        assertTrue(countDocuments() == 1);
    }

    @Test
    public void validateQuestion() throws JsonProcessingException {
        // these tests could fail if someone changes the question...
        Question question = retrieverCtl.fetchOneQuestion("stackoverflow", TEST_QU_ID);
        assertEquals(question.title(), TEST_QU_TITLE);
        assertEquals(question.tags(), TEST_TAGS_LIST);
        assertEquals(question.comments(), null);
        assertEquals(question.answers().size(), 4);
        assertEquals(question.answers().get(0).comments().size(), 2);
    }

    private final WsMessageContext mockWsCtx = mock(WsMessageContext.class);

    private void testWebsocketLoadData(String question_site, List<Integer> question_ids,
            String message) throws SQLException, URISyntaxException, IOException,
            InterruptedException {
        QuestionLoadList qLL = new QuestionLoadList(question_site, question_ids);

        when(mockWsCtx.messageAsClass(QuestionLoadList.class)).thenReturn(qLL);
        new LoaderController(qdb).websocketLoadData(mockWsCtx);
        verify(mockWsCtx).send(message);
    }

    private void deleteDocuments() {
        Bson query = gt("question_id", -999);
        qdb.collection().deleteMany(query);
    }

    private long countDocuments() {
        CountOptions opts = new CountOptions().hintString("_id_");
        return qdb.collection().countDocuments(new BsonDocument(), opts);
    }

}
