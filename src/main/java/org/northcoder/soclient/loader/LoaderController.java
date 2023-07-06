package org.northcoder.soclient.loader;

import io.javalin.http.Context;
import io.javalin.websocket.WsMessageContext;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.northcoder.soclient.QuestionsDB;
import org.northcoder.soclient.Site;
import org.northcoder.soclient.Util;
import org.northcoder.soclient.records.QuestionLoadList;
import org.northcoder.soclient.records.ValidationResult;

public class LoaderController {

    private final QuestionsDB qdb;

    public LoaderController(QuestionsDB qdb) {
        this.qdb = qdb;
    }

    public void loadDataList(Context ctx) {
        Map<String, Object> model = new HashMap<>();
        model.put("sites", Site.class);
        ctx.render("load_data.html", model);
    }

    public void websocketLoadData(WsMessageContext wsCtx) throws SQLException,
            URISyntaxException, IOException, InterruptedException {
        ValidationResult result = parseQuestionLoadList(wsCtx);
        if (result.validationResult().equals(Util.OK)) {
            new DataLoader(wsCtx, qdb).loadQuestionData(result.qLL());
            wsCtx.send(Util.OK_FINISHED);
        } else {
            wsCtx.send(result.validationResult());
        }
        wsCtx.closeSession();
    }

    private ValidationResult parseQuestionLoadList(WsMessageContext wsCtx) {
        QuestionLoadList qLL = null;
        try {
            qLL = wsCtx.messageAsClass(QuestionLoadList.class);
        } catch (Exception e) {
            // for something unexpected:
            return new ValidationResult(qLL, e.getMessage());
        }
        return validateQuestionLoadList(qLL);
    }

    private ValidationResult validateQuestionLoadList(QuestionLoadList qLL) {
        if (qLL.question_site() == null || qLL.question_site().isBlank()) {
            return new ValidationResult(qLL, Util.ERR_NO_SITE);
        }
        if (!Site.exists(qLL.question_site().trim())) {
            return new ValidationResult(qLL, Util.ERR_INVALID_SITE);
        }
        // remove any null IDs:
        while (qLL.question_ids().remove(null)) {
            // no logic needed here
        }
        // not comprehensive - just the most likely problems:
        if (qLL.question_ids().isEmpty()
                || (qLL.question_ids().size() == 1
                && (qLL.question_ids().get(0) == null
                || qLL.question_ids().get(0) <= 0))) {
            return new ValidationResult(qLL, Util.ERR_NO_IDS);
        }
        // the API suports lists of up to 100 items, but we use 30, to
        // allow for also retrieving comments separately:
        if (qLL.question_ids().size() > Util.MAX_IDS) {
            return new ValidationResult(qLL, Util.ERR_TOO_MANY_IDS);
        }
        return new ValidationResult(qLL, Util.OK);

    }

}
