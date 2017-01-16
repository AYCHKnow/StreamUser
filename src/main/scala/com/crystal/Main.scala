package com.crystal

// Stream Processors
import processors.SnowplowStreamProcessor
import processors.CommandStreamProcessor

// Spark
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{ Duration, StreamingContext }

object Main extends App {
  AppConfig.setArgs(args)

  AppConfig.load() match {
    case Some(appConfig) =>
      val sparkConf = new SparkConf()
        .setMaster("local[*]")
        .setAppName(appConfig.appName)

      val streamingCtx = new StreamingContext(
        sparkConf,
        Duration(appConfig.checkpointInterval)
      )

      // Disable noisy logging
      streamingCtx.sparkContext.setLogLevel("ERROR")

      SnowplowStreamProcessor.setup(streamingCtx, appConfig)
      CommandStreamProcessor.setup(appConfig)

      streamingCtx.start()

      streamingCtx
        .awaitTerminationOrTimeout(appConfig.checkpointInterval)

    case None => ()
  }
}
