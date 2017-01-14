package com.crystal

// Models
import models.User

// Spark
import org.apache.spark.SparkConf
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.{ Duration, StreamingContext }
import org.apache.spark.streaming.kinesis._
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.InitialPositionInStream

// JSON Parsing
import scala.util.parsing.json.JSON

object Main extends App {
  AppConfig.setArgs(args)

  AppConfig.load() match {
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
        .map { e => (e.get(appConfig.userIdentifier).get.asInstanceOf[String], Array(e)) }
        .reduceByKey { (a, b) => a.union(b) }
        .map {
          case (user_id, eventList) =>
            eventList.foldLeft(User.withID(user_id)) { (b, a) => b.performedAction(a) }
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
