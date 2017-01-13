# Streaming User Segmentation

*In Development - Not fully functional*<br><br>

Will combine the following technologies:

- **Snowplow Analytics** (utilizing Kinesis streams) - https://github.com/snowplow/snowplow


- **Apache Spark** (for real-time stream processing) - http://spark.apache.org/


<br>

Performing User Segmentation on our event-level analytics. Communicating the segmentation to external APIs.


<br><br>

# Development Setup (Docker)

1. Create **src/main/resources/application.conf** with the proper configuration

1. Set `AWS_ACCESS_KEY_ID` and `AWS_SECRET_ACCESS_KEY` environment variables

1. Install docker and docker-compose

1. `docker-compose up`

<br><br>

# Development Setup (Mac)

1. Install Homebrew (if not installed)

1. `brew cask install java` (if Java is not installed)

1. `brew install scala` (if Scala is not installed)

1. `brew install sbt` (if SBT is not installed)

1. `git clone git@github.com:crystal-project-inc/streaming_user_segmentation.git`

1. Create **src/main/resources/application.conf** with the proper configuration

1. Configure **aws-cli** on your machine (to connect to your AWS account)

1. Run `sbt run` from project's root directory



<br><br>

# Working so far:

1. Reading enriched JSON Snowplow data from Kinesis

1. Real-time enrichment via historical DynamoDB data

<br>

# TODO:

1. Proper identity stitching

1. Dynamic Segmentation Rules

1. Responder logic for segmentation rules
