package com.crystal

// akka
import akka.actor._
import akka.event.Logging

// Spark
import org.apache.spark.streaming.{ StreamingContext }

// Processors
import processors.SnowplowStreamProcessor
import processors.CommandStreamProcessor

class Overseer(appConfig: AppConfig, streamingCtx: StreamingContext) extends Actor {
  import Overseer._
  val log = Logging(context.system, this)

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
  log.info("Overseer initialized. Waiting on processors")

  def receive = {
    case ProcessorReady(processor) =>
      if (processor == snowplowStream) {
        log.info("Overseer learned Snowplow Processor is ready")
        snowplowReady = true
      }

      if (processor == commandStream) {
        log.info("Overseer learned Command Processor is ready")
        commandReady = true
      }

      if (snowplowReady && commandReady) {
        log.info("Overseer starting Spark")
        startSpark()
      }
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
