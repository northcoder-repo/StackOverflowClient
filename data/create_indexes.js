//
// Create an extra index on the main and test collections
//
let qdb = connect( 'mongodb://localhost:27017/stackoverflow' );
let questions = qdb.getCollection("questions");
questions.createIndex (
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

let testdb = connect( 'mongodb://localhost:27017/test' );
let test_questions = testdb.getCollection("test_questions");
test_questions.createIndex (
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
