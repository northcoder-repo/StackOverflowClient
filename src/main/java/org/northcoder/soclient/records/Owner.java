package org.northcoder.soclient.records;

public record Owner(
        int user_id,
        int reputation,
        String display_name,
        String content_license) {

}
