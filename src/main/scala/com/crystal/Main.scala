package com.crystal

// Spark
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{ Duration, StreamingContext }

// Processors
import processors.CommandStreamProcessor
import processors.SnowplowStreamProcessor

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

      CommandStreamProcessor.setup(appConfig, streamingCtx)
      SnowplowStreamProcessor.setup(appConfig, streamingCtx)

      streamingCtx.start()
      streamingCtx.awaitTerminationOrTimeout(appConfig.checkpointInterval * 3)
    case None => ()
  }
}
