package com.crystal
package stores

// AWS SDK
import awscala.dynamodbv2.{ DynamoDB => DynamoDBv2, _}
import awscala.Region
import com.amazonaws.services.dynamodbv2.model.Condition

case class DynamoDB(table: Table) {
  import DynamoDB._

  def find(id: String): Option[Item] = {
    implicit val dynamoDB = DynamoDBv2.at(region)
    table.get(id)
  }

  def findAll(id: String, limit: Int = 10000): Seq[Item] = {
    implicit val dynamoDB = DynamoDBv2.at(region)
    table.query(Seq(table.hashPK -> cond.eq(id)), consistentRead = true, limit = limit)
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

  val userActionTable = Table(
    config.userTable,
    hashPK = "user_id",
    rangePK = Some("tstamp"),
    attributes = Seq(
      AttributeDefinition("user_id", AttributeType.String),
      AttributeDefinition("tstamp", AttributeType.String)
    )
  )
}
