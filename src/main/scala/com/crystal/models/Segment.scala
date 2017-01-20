package com.crystal
package models

// Kinesis
import stores.KinesisStream

// JSON
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.{read, write}

case class Segment(val name: String) {
  import Segment._

  def containsUser(user: User): Boolean = {
    return true
  }

  def publishUserEntrance(user: User) = {
    implicit val formats = Serialization.formats(ShortTypeHints(List(classOf[EnterSegmentEvent])))

    val event = EnterSegmentEvent(user.id, name)
    stream.put(user.id, write(event).getBytes("UTF-8"))
  }

  def publishUserExit(user: User) = {
    implicit val formats = Serialization.formats(ShortTypeHints(List(classOf[ExitSegmentEvent])))

    val event = ExitSegmentEvent(user.id, name)
    stream.put(user.id, write(event).getBytes("UTF-8"))
  }
}

object Segment {
  val config = AppConfig.load().get
  val stream = KinesisStream(config.outStreamName)

  case class EnterSegmentEvent(user_id: String, segment_name: String)
  case class ExitSegmentEvent(user_id: String, segment_name: String)
}
