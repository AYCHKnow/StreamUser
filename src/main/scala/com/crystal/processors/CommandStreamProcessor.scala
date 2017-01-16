package com.crystal
package processors

// scala
import scala.language.postfixOps
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

// amazonaws
import com.amazonaws.auth.{DefaultAWSCredentialsProviderChain}
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.KinesisClientLibConfiguration
import com.amazonaws.services.kinesis.model.Record

// kinesis
import com.gilt.gfc.aws.kinesis.client.{KCLConfiguration, KCLWorkerRunner, KinesisRecordReader}

/*
 * TODO: This should use Spark in the future
 *       Spark currently does not allow processing of multiple kinesis streams
 */

object CommandStreamProcessor {
  def setup(appConfig: AppConfig) = {
    val kclConfig = getKclConfig(appConfig)

    val thread = new Thread(new Runnable {
      def run() {
        implicit object ARecordReader extends KinesisRecordReader[Record]{
          override def apply(r: Record) : Record = {
            r
          }
        }

        KCLWorkerRunner(kclConfig).runAsyncSingleRecordProcessor[Record](1 minute) { record: Record =>
          Future {
            val seqNumber = record.getSequenceNumber()
            val data = record.getData()

            println("Command Received")
          }
        }
      }
    })

    thread.start
  }

  private def getKclConfig(appConfig: AppConfig): KinesisClientLibConfiguration = {
    val credentialsProvider = new DefaultAWSCredentialsProviderChain()

    KCLConfiguration(
      appConfig.commandAppName,
      appConfig.commandStreamName,
      credentialsProvider,
      credentialsProvider,
      credentialsProvider
    ).withRegionName(appConfig.regionName)
  }
}
