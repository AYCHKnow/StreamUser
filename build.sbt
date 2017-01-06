name := "Snowplow Stream Processor"

version := "1.0"

scalaVersion := "2.11.1"

sbtVersion := "0.13.13"

libraryDependencies += "org.apache.spark" %% "spark-streaming-kinesis-asl" % "2.1.0"
libraryDependencies += "com.typesafe" % "config" % "1.3.1"
libraryDependencies += "com.amazonaws" % "amazon-kinesis-client" % "1.7.2"
