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


  val userStream = eventStream
    .map { parsed =>
      val user_id = parsed.get("network_userid").get.asInstanceOf[String]
      val action_name = parsed.get("se_action").get.asInstanceOf[String]

      User.withID(user_id).performedAction(action_name)
    }

  userStream.foreachRDD { rdd =>
    rdd.collect().foreach{ user =>
      println(user.id)
      user.save()
    }
  }

  streamingCtx.start()

  streamingCtx
    .awaitTerminationOrTimeout(config.getInt("checkpoint_interval"))
}
