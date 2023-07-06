package org.northcoder.soclient.records;

import java.util.List;

public record Questions(
        List<Question> items,
        boolean has_more,
        int quota_remaining) {

}
