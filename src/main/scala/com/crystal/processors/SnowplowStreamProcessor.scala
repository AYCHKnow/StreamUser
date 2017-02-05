package com.crystal
package processors

// Spark
import org.apache.spark.streaming.kinesis._
import org.apache.spark.streaming.{ Duration, StreamingContext }
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.storage.StorageLevel
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.InitialPositionInStream

// JSON Parsing
import com.snowplowanalytics.snowplow.analytics.scalasdk.json.EventTransformer
import scala.util.parsing.json.JSON

// Models
import models.User
import models.Segment

// Segmentation Rules
import rule_engine.rules._

object SnowplowStreamProcessor {
  def setup(appConfig: AppConfig, streamingCtx: StreamingContext) = {
    val snowplowStream = getSnowplowStream(appConfig, streamingCtx)
    val evtStream = getEventStream(snowplowStream)
    val userStream = getUserStream(evtStream, appConfig.userIdentifier)

    userStream.foreachRDD { rdd =>
      rdd.foreach{ user =>
        val signedUpRule = ContainsAllRule(
          property = Some("actions"),
          ruleset = Vector(
            EqualsRule(property = Some("action"), value = Some("Signed up"))
          )
        )

        val testSegment = new Segment("SignedUp", signedUpRule)

        val wasInSegment = testSegment.alreadyContainedUser(user)
        val isInSegment = testSegment.containsUser(user)
        if (isInSegment && !wasInSegment) {
          println(s"${user.id} is now in segment ${testSegment.name}")
          testSegment.publishUserEntrance(user)

        } else if(!isInSegment && wasInSegment) {
          println(s"${user.id} is no longer in ${testSegment.name}")
          testSegment.publishUserExit(user)

        } else if(wasInSegment) {
          println(s"${user.id} is still in ${testSegment.name}")
        } else {
          println(s"${user.id} is not in ${testSegment.name}")
        }

        user.save()
      }
    }
  }


  private def getSnowplowStream(appConfig: AppConfig, streamingCtx: StreamingContext): DStream[Array[Byte]] = {
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
