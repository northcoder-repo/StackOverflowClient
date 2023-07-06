package org.northcoder.soclient;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

//
// https://www.mongodb.com/docs/drivers/java/sync/current/usage-examples
//
public record QuestionsDB(
        String uri,
        String dbName,
        String collectionName,
        MongoCollection<Document> collection) {

    public QuestionsDB(String uri, String dbName, String collectionName) {
        this(uri, dbName, collectionName,
                createCollection(uri, dbName, collectionName));
    }

    private static MongoCollection<Document> createCollection(String uri,
            String dbName, String collectionName) {
        return MongoClients
                .create(uri)
                .getDatabase(dbName)
                .getCollection(collectionName);
    }
}
