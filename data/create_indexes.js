//
// Create an extra index on the main and test collections
//

let url = "mongodb://localhost:27017/";

let connections = [
  {
    "dbName": "stackoverflow",
    "collectionName": "questions"
  },
  {
    "dbName": "test",
    "collectionName": "test_questions"
  }
];

connections.forEach(conn => createIndex(conn));

function createIndex(conn) {
  let db = connect(url + conn.dbName);
  let collection = db.getCollection(conn.collectionName);
  collection.createIndex (
    {
      "question_id": 1, 
      "site": 1
    },
    {
      unique: true,
      sparse: true,
      name: "qu_and_site"
    }
  );
}
