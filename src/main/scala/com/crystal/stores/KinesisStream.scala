package com.crystal
package stores

// Kinesis
import jp.co.bizreach.kinesis._
import com.amazonaws.regions.Regions
import java.nio.ByteBuffer

case class KinesisStream(name: String) {
  import KinesisStream._

  def put(key: String, data: Array[Byte]) = {
    val request = PutRecordRequest(
      streamName = name,
      partitionKey = key,
      data = data
    )

    client.putRecordWithRetry(request)
  }
}

object KinesisStream {
  val config = AppConfig.load().get
  implicit val region = Regions.fromName(config.regionName)
  val client = AmazonKinesisClient()
}
