package com.crystal
package models

// Kinesis
import stores.KinesisStream

case class Segment(val name: String) {
  import Segment._

  def containsUser(user: User): Boolean = {
    return true
  }

  def publishUserEntrance(user: User) = {
    val event = EnterSegmentEvent(user.id, name)

    //TODO: Json Encoding
    stream.put(user.id, "{\"test\": true}".getBytes("UTF-8"))
  }
}

object Segment {
  val config = AppConfig.load().get
  val stream = KinesisStream(config.outStreamName)

  case class EnterSegmentEvent(user_id: String, segment_name: String)
}
