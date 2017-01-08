package com.crystal
package stores

// AWS SDK
import awscala.dynamodbv2.{ DynamoDB => DynamoDBv2, _}
import awscala.Region

// Config
import com.typesafe.config.ConfigFactory

case class DynamoDB(table: Table) {
  import DynamoDB._

  def find(id: String): Option[Item] = {
    implicit val dynamoDB = DynamoDBv2.at(region)
    table.get(id)
  }

  def save(id: String, attributes: (String, Any)*) = {
    implicit val dynamoDB = DynamoDBv2.at(region)
    table.put(id, attributes)
  }
}

object DynamoDB {
  val config = ConfigFactory.load()
  val region = Region(config.getString("region_name"))
  implicit val dynamoDB = DynamoDBv2.at(region)

  val userTable = Table(
    config.getString("user_table"),
    hashPK = "id",
    attributes = Seq(
      AttributeDefinition("id", AttributeType.String)
    )
  )
}
