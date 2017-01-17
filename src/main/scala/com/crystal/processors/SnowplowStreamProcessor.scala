package com.crystal
package processors

// akka
import akka.actor._

// Models
import models.User
import models.Segment

// Spark
import org.apache.spark.streaming.kinesis._
import org.apache.spark.streaming.{ Duration, StreamingContext }
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.storage.StorageLevel
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.InitialPositionInStream

// JSON Parsing
import com.snowplowanalytics.snowplow.analytics.scalasdk.json.EventTransformer
import scala.util.parsing.json.JSON

// Messages
import Overseer.ProcessorReady

class SnowplowStreamProcessor(appConfig: AppConfig, streamingCtx: StreamingContext) extends Actor {
  import SnowplowStreamProcessor._

  override def preStart() = {
    super.preStart()

    val snowplowStream = getSnowplowStream(streamingCtx, appConfig)
    val evtStream = getEventStream(snowplowStream)
    val userStream = getUserStream(evtStream, appConfig.userIdentifier)

    val testSegment = new Segment("testSegment")

    userStream.foreachRDD { rdd =>
      rdd.collect().foreach{ user =>
        if (testSegment.containsUser(user)) {
          println(s"${user.id} is in segment ${testSegment.name}")
          testSegment.publishUserEntrance(user)
        }

        user.save()
      }
    }

    context.parent ! ProcessorReady(self)
  }

  def receive = {
    case _ => ()
  }

  private def getSnowplowStream(streamingCtx: StreamingContext, appConfig: AppConfig): DStream[Array[Byte]] = {
    KinesisUtils.createStream(
      streamingCtx,
      appConfig.appName,
      appConfig.streamName,
      s"kinesis.${appConfig.regionName}.amazonaws.com",
      appConfig.regionName,
      InitialPositionInStream.LATEST,
      Duration(appConfig.checkpointInterval),
      StorageLevel.MEMORY_AND_DISK_2
    )
  }

  private def getEventStream(snowplowStream: DStream[Array[Byte]]): DStream[Map[String, Any]] = {
    snowplowStream
      .map { byteArray => new String(byteArray) }
      .map(line => EventTransformer.transform(line))
      .filter(_.isSuccess)
      .flatMap(_.toOption)
      .map { jsonStr => JSON.parseFull(jsonStr).get.asInstanceOf[Map[String, Any]] }
  }


  private def getUserStream(evtStream: DStream[Map[String, Any]], user_identifier: String): DStream[User] = {
    val userStream = evtStream
      .map { e => (e.get(user_identifier).get.asInstanceOf[String], Array(e)) }
      .reduceByKey { (a, b) => a.union(b) }
      .map {
        case (user_id, eventList) =>
          eventList.foldLeft(User.withID(user_id)) { (u, a) => u.performedAction(a) }
      }


      userStream.filter { user => !user.id.isEmpty }
  }
}

object SnowplowStreamProcessor {
  def props(appConfig: AppConfig, streamingCtx: StreamingContext): Props = {
    Props(new SnowplowStreamProcessor(appConfig, streamingCtx))
  }
}
