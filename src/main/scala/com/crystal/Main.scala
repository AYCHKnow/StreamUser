package com.crystal

// Models
import models.User

// Config
import com.typesafe.config.ConfigFactory

// Spark
import org.apache.spark.SparkConf
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.{ Duration, StreamingContext }
import org.apache.spark.streaming.kinesis._
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.InitialPositionInStream

// JSON Parsing
import scala.util.parsing.json.JSON


case class Config(streamName: String = "", appName: String = "", regionName: String = "",
                  checkpointInterval: Int = -1, userTable: String = "", userIdentifier: String = "")

object Main extends App {
  val parser = new scopt.OptionParser[Config]("scopt") {
    head("streaming_user_segmentation", "0.x")

    opt[String]('s', "streamName").action( (x, c) =>
      c.copy(streamName = x) ).text("name of snowplow kinesis stream")

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
  val initialArgs = if (config.isEmpty()) {
    Config()
  } else {
    Config(
      streamName = config.getString("stream_name"),
      appName = config.getString("app_name"),
      regionName = config.getString("region_name"),
      checkpointInterval = config.getInt("checkpoint_interval"),
      userTable = config.getString("user_table"),
      userIdentifier = config.getString("user_identifier")
    )
  }

  parser.parse(args, initialArgs) match {
    case Some(appConfig) =>
      val sparkConf = new SparkConf()
        .setMaster("local[2]")
        .setAppName(appConfig.appName)

      val streamingCtx = new StreamingContext(
        sparkConf,
        Duration(appConfig.checkpointInterval)
      )

      val kinesisStream = KinesisUtils.createStream(
        streamingCtx,
        appConfig.appName,
        appConfig.streamName,
        s"kinesis.${appConfig.regionName}.amazonaws.com",
        appConfig.regionName,
        InitialPositionInStream.LATEST,
        Duration(appConfig.checkpointInterval),
        StorageLevel.MEMORY_AND_DISK_2
      )

      val eventStream = kinesisStream
        .map { byteArray => new String(byteArray) }
        .map { stringVal => JSON.parseFull(stringVal).get.asInstanceOf[Map[String, Any]] }


      val userStream = eventStream
        .map { parsed =>
          val user_id = parsed.get(appConfig.userIdentifier).get.asInstanceOf[String]

          User.withID(user_id).performedAction(parsed)
        }

      userStream.foreachRDD { rdd =>
        rdd.collect().foreach{ user =>
          println(user.id)
          user.save()
        }
      }

      streamingCtx.start()

      streamingCtx
        .awaitTerminationOrTimeout(appConfig.checkpointInterval)

    case None => ()
  }
}
