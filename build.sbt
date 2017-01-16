enablePlugins(JavaServerAppPackaging)

name := "streaming-user-segmentation"

version := "0.1"

scalaVersion := "2.11.1"

sbtVersion := "0.13.13"

resolvers ++= Seq(
  "Snowplow Analytics" at "http://maven.snplow.com/releases/"
)

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-streaming-kinesis-asl" % "2.1.0",
  "com.snowplowanalytics" %% "snowplow-scala-analytics-sdk" % "0.1.0",
  "com.gilt" %% "gfc-aws-kinesis" % "0.10.2",
  "com.gilt" %% "gfc-aws-kinesis-akka" % "0.10.2",
  "com.github.seratch" %% "awscala" % "0.5.9",
  "jp.co.bizreach" %% "aws-kinesis-spark" % "0.0.3",
  "com.amazonaws" % "amazon-kinesis-client" % "1.7.2",
  "com.typesafe" % "config" % "1.3.1",
  "com.github.scopt" %% "scopt" % "3.5.0",
  "org.scalatest" %% "scalatest" % "2.2.0" % "test",
  "org.scalamock" %% "scalamock-scalatest-support" % "3.2.2" % "test"
)

packageName in Universal := "deploy"
topLevelDirectory := None
