package com.crystal
package stores

// AWS SDK
import awscala.dynamodbv2.{ DynamoDB => DynamoDBv2, _}
import awscala.Region

case class DynamoDB(table: Table) {
  import DynamoDB._

  def find(id: String): Option[Item] = {
    implicit val dynamoDB = DynamoDBv2.at(region)
    table.get(id)
  }

  def save(id: String, attributes: (String, Any)*) = {
    implicit val dynamoDB = DynamoDBv2.at(region)
    table.put(id, attributes:_*)
  }
}

object DynamoDB {
  val config = AppConfig.load().get
  val region = Region(config.regionName)
  implicit val dynamoDB = DynamoDBv2.at(region)

  val userTable = Table(
    config.userTable,
    hashPK = "id",
    attributes = Seq(
      AttributeDefinition("id", AttributeType.String)
    )
  )
}
