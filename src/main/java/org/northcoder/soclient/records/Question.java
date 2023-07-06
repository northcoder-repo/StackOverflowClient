package org.northcoder.soclient.records;

import java.util.List;
import org.northcoder.soclient.Util;

public record Question(
        int question_id,
        Owner owner,
        long creation_date,
        int score,
        String title,
        String body,
        List<Comment> comments,
        List<String> tags,
        List<Answer> answers,
        String content_license,
        String creation_datetime,
        String link,
        String site)
        implements Item {

    public Question             {
        // used by Jackson:
        creation_datetime = Util.formatDateTime(creation_date);
        site = link.substring(8, link.indexOf('.'));
    }

    public Question(
            int question_id,
            Owner owner,
            long creation_date,
            int score,
            String title,
            String body,
            List<Comment> comments,
            List<String> tags,
            List<Answer> answers,
            String content_license,
            String link) {
        // format an additional creation_datetime string and site string:
        this(question_id, owner, creation_date, score, title, body, comments, tags,
                answers, content_license, link, Util.formatDateTime(creation_date),
                link.substring(8, link.indexOf('.')));
    }
}
