package com.crystal
package processors

// Spark
import org.apache.spark.streaming.kinesis._
import org.apache.spark.streaming.{ Duration, StreamingContext }
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.storage.StorageLevel
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.InitialPositionInStream

// JSON Parsing
import scala.util.parsing.json.JSON

object CommandStreamProcessor {
  def setup(appConfig: AppConfig, streamingCtx: StreamingContext) = {
    val cmdStream = getCommandStream(appConfig, streamingCtx)

    cmdStream.foreachRDD { rdd =>
      rdd.foreach{ cmd =>
        println("--- Command Received ---")
      }
    }
  }

  private def getCommandStream(
    appConfig: AppConfig,
    streamingCtx: StreamingContext): DStream[Map[String, Any]] = {
    val stream = KinesisUtils.createStream(
      streamingCtx,
      appConfig.commandAppName,
      appConfig.commandStreamName,
      s"kinesis.${appConfig.regionName}.amazonaws.com",
      appConfig.regionName,
      InitialPositionInStream.LATEST,
      Duration(appConfig.checkpointInterval),
      StorageLevel.MEMORY_AND_DISK_2
    )

    stream
      .map { byteArray => new String(byteArray) }
      .map { jsonStr => JSON.parseFull(jsonStr).get.asInstanceOf[Map[String, Any]] }
  }

}
