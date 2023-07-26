# Stack Overflow Client
 
A basic personal project to try MongoDB. 

It uses the Stack Exchange API as a data source. 

The web app allows me to store a curated set of questions locally, pulled from various [Stack Exchange sites](https://github.com/northcoder-repo/StackOverflowClient/blob/main/src/main/java/org/northcoder/soclient/Site.java) using their [public API](https://api.stackexchange.com/docs). Volumes are small and read-only, so there is [no need to register](https://stackapps.com/) or [authenticate](https://api.stackexchange.com/docs/authentication) my application. I just use their anonymous usage, with its (300 calls per day per site).

For the MongoDB, I use the community download version 6.0.6, which includes Compass:

https://www.mongodb.com/try/download/community

My MongoDB collections (`questions` and `test_questions`) use one additional compound index on `question_id` and `site`. I use the `mongosh` command line utility to automate [creation of the index](https://github.com/northcoder-repo/StackOverflowClient/blob/main/data/10_mongosh_create_indexes.bat).

Downloadable from here (I use v 1.10.1):

https://www.mongodb.com/try/download/shell

I also use the separate command line tools for collection import/export utilities:

https://www.mongodb.com/try/download/database-tools

Specificially, this is version 100.7.3.
