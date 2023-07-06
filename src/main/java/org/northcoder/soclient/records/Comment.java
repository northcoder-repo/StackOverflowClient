package org.northcoder.soclient.records;

import org.northcoder.soclient.Util;

public record Comment(
        long post_id,
        long comment_id,
        long creation_date,
        Owner owner,
        int score,
        String body,
        String content_license,
        String creation_datetime) {

    public Comment        {
        // used by Jackson:
        creation_datetime = Util.formatDateTime(creation_date);
    }

    public Comment(
            int post_id,
            long comment_id,
            long creation_date,
            Owner owner,
            int score,
            String body,
            String content_license) {
        // format an additional creation_datetime string:
        this(post_id, comment_id, creation_date, owner, score, body,
                content_license, Util.formatDateTime(creation_date));
    }
}
