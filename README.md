# StackOverflowClient
 
A basic personal project to try MongoDB. Uses the Stack Exchange API as a data source. The web app allows me to store a curated set of questions locally, pulled from various Stack Exchange sites using their [public API](https://api.stackexchange.com/docs). Volumes are small, so no need to register an app with SE. I just use their anonymous usage, with its limited quotas (300 calls per day per site).

The MongoDB collections (`questions` and `test_questions`) use one additional compound index on `question_id` and `site`.
