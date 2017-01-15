package com.crystal
package models

// Kinesis
import stores.KinesisStream

// JSON
import argonaut._, Argonaut._

case class Segment(val name: String) {
  import Segment._

  def containsUser(user: User): Boolean = {
    return true
  }

  def publishUserEntrance(user: User) = {
    val event = EnterSegmentEvent(user.id, name)
    stream.put(user.id, event.asJson.toString.getBytes("UTF-8"))
  }
}

object Segment {
  val config = AppConfig.load().get
  val stream = KinesisStream(config.outStreamName)

  case class EnterSegmentEvent(user_id: String, segment_name: String)
  implicit def EnterSegmentEventEncodeJson: EncodeJson[EnterSegmentEvent] =
    EncodeJson((e: EnterSegmentEvent) =>
      ("user_id" := e.user_id) ->:
      ("segment_name" := e.segment_name) ->:
      ("event_name" := "entered") ->: jEmptyObject
    )
}
