package com.crystal

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

object Main extends App {
  val config = ConfigFactory.load()

  val sparkConf = new SparkConf()
    .setMaster("local[2]")
    .setAppName(config.getString("app_name"))

  val streamingCtx = new StreamingContext(
    sparkConf,
    Duration(config.getInt("checkpoint_interval"))
  )

  val kinesisStream = KinesisUtils.createStream(
    streamingCtx,
    config.getString("app_name"),
    config.getString("stream_name"),
    config.getString("endpoint_url"),
    config.getString("region_name"),
    InitialPositionInStream.LATEST,
    Duration(config.getInt("checkpoint_interval")),
    StorageLevel.MEMORY_AND_DISK_2
  )

  val eventStream = kinesisStream
    .map { byteArray => new String(byteArray) }
    .map { stringVal => JSON.parseFull(stringVal).get.asInstanceOf[Map[String, Any]] }
    .map { parsed => (
      parsed.get("se_action").get,
      parsed.get("se_label").get,
      parsed.get("domain_userid").get
    )}

  eventStream.print()

  streamingCtx.start()

  streamingCtx
    .awaitTerminationOrTimeout(config.getInt("checkpoint_interval"))
}
