package org.northcoder.soclient.records;

import java.util.List;
import org.northcoder.soclient.Util;

public record Answer(
        int answer_id,
        Owner owner,
        long creation_date,
        int score,
        boolean is_accepted,
        List<Comment> comments,
        String body,
        String content_license,
        String creation_datetime)
        implements Item {

    public Answer         {
        // used by Jackson:
        creation_datetime = Util.formatDateTime(creation_date);
    }

    public Answer(
            int answer_id,
            Owner owner,
            long creation_date,
            int score,
            boolean is_accepted,
            List<Comment> comments,
            String body,
            String content_license) {
        // format an additional creation_datetime string:
        this(answer_id, owner, creation_date, score, is_accepted, comments,
                body, content_license, Util.formatDateTime(creation_date));
    }
}
