package org.northcoder.soclient.records;

import java.util.List;

public record Comments(
        List<Comment> items,
        boolean has_more,
        int quota_remaining) {

}
