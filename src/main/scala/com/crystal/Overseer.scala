package com.crystal

// akka
import akka.actor._

// Spark
import org.apache.spark.streaming.{ StreamingContext }

// Processors
import processors.SnowplowStreamProcessor
import processors.CommandStreamProcessor

class Overseer(appConfig: AppConfig, streamingCtx: StreamingContext) extends Actor {
  import Overseer._

  val commandStream = context.actorOf(
    CommandStreamProcessor.props(appConfig, streamingCtx),
    "commandStream"
  )

  val snowplowStream = context.actorOf(
    SnowplowStreamProcessor.props(appConfig, streamingCtx),
    "snowplowStream"
  )

  var snowplowReady = false
  var commandReady = false

  def receive = {
    case ProcessorReady(processor) =>
      if (processor == snowplowStream) snowplowReady = true
      if (processor == commandStream) commandReady = true

      if (snowplowReady && commandReady) startSpark()
    case _ => ()
  }

  def startSpark() = {
    streamingCtx.start()
    streamingCtx.awaitTerminationOrTimeout(appConfig.checkpointInterval * 3)
  }
}

object Overseer {
  def props(appConfig: AppConfig, streamingCtx: StreamingContext): Props = {
    Props(new Overseer(appConfig, streamingCtx))
  }

  case class ProcessorReady(processor: ActorRef)
}
