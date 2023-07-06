package org.northcoder.soclient.retriever;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.javalin.http.Context;
import java.util.HashMap;
import java.util.Map;
import org.northcoder.soclient.QuestionsDB;
import org.northcoder.soclient.records.Question;

public class RetrieverController {

    private final QuestionsDB qdb;

    public RetrieverController(QuestionsDB qdb) {
        this.qdb = qdb;
    }

    public void fetchQuestions(Context ctx) {
        Map<String, Object> model = new HashMap<>();
        ctx.render("questions.html", model);
    }

    public void fetchOneQuestion(Context ctx) throws JsonProcessingException {
        String site = ctx.pathParam("site");
        int questionId = Integer.parseInt(ctx.pathParam("question_id"));
        Question question = fetchOneQuestion(site, questionId);
        Map<String, Object> model = new HashMap<>();
        model.put("question", question);
        ctx.render("question.html", model);
    }

    public Question fetchOneQuestion(String site, int questionId)
            throws JsonProcessingException {
        return new DataRetriever(qdb).getQuestionDetails(site, questionId);
    }

    public void fetchQuestionsJson(Context ctx) throws JsonProcessingException {
        ctx.json(new DataRetriever(qdb).getAllQuestions());
    }

}
