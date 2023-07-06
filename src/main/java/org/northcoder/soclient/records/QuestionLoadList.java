package org.northcoder.soclient.records;

import java.util.List;

public record QuestionLoadList(
        String question_site,
        List<Integer> question_ids) {

}
