package com.crystal

// Config
import com.typesafe.config.ConfigFactory

case class AppConfig(streamName: String, outStreamName: String, appName: String, regionName: String,
                     checkpointInterval: Int, userTable: String, userIdentifier: String)

object AppConfig {
  private var cliArgs: Array[String] = Array()

  def setArgs(args: Array[String]) {
    cliArgs = args
  }

  def load(): Option[AppConfig] = {
    val parser = new scopt.OptionParser[AppConfig]("scopt") {
      head("streaming_user_segmentation", "0.x")

      opt[String]('s', "streamName").action( (x, c) =>
        c.copy(streamName = x) ).text("name of input snowplow kinesis stream")

      opt[String]('o', "outStreamName").action( (x, c) =>
        c.copy(outStreamName = x) ).text("name of output snowplow kinesis stream")

      opt[String]("appName").action( (x, c) =>
        c.copy(appName = x) ).text("name of (this) segmentation application")

      opt[String]('r', "regionName").action( (x, c) =>
        c.copy(regionName = x) ).text("AWS region containing streams/dynamodb")

      opt[Int]('c', "checkpointInterval").action( (x, c) =>
        c.copy(checkpointInterval = x) ).text("spark checkpointing/batch interval")

      opt[String]("userTable").action( (x, c) =>
        c.copy(userTable = x) ).text("table in which to store aggregated user data")

      opt[String]("userIdentifier").action( (x, c) =>
        c.copy(userIdentifier = x) ).text("snowplow event property used to identify users (i.e. domain_userid)")
    }

    val config = ConfigFactory.load()
    val initialArgs = AppConfig(
      streamName = config.getString("stream_name"),
      outStreamName = config.getString("out_stream_name"),
      appName = config.getString("app_name"),
      regionName = config.getString("region_name"),
      checkpointInterval = config.getInt("checkpoint_interval"),
      userTable = config.getString("user_table"),
      userIdentifier = config.getString("user_identifier")
    )

    parser.parse(cliArgs, initialArgs)
  }
}
