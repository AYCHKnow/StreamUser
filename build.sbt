enablePlugins(JavaServerAppPackaging)

name := "streaming-user-segmentation"

version := "0.1"

scalaVersion := "2.11.1"

sbtVersion := "0.13.13"


libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-streaming-kinesis-asl" % "2.1.0",
  "com.github.seratch" %% "awscala" % "0.5.9",
  "jp.co.bizreach" %% "aws-kinesis-spark" % "0.0.3",
  "com.amazonaws" % "amazon-kinesis-client" % "1.7.2",
  "com.typesafe" % "config" % "1.3.1",
  "io.argonaut" %% "argonaut" % "6.1",
  "com.github.scopt" %% "scopt" % "3.5.0",
  "org.scalatest" %% "scalatest" % "2.2.0" % "test",
  "org.scalamock" %% "scalamock-scalatest-support" % "3.2.2" % "test"
)

packageName in Universal := "deploy"
topLevelDirectory := None
