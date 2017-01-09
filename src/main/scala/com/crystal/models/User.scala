package com.crystal
package models

// Scala
import scala.collection.immutable.Queue

// Persistent Storage
import stores.DynamoDB
import awscala.dynamodbv2.AttributeType

case class User(val id: String, actions: Queue[Map[String, String]] = Queue()) {
  import User._

  def performedAction(action: Map[String, Any]): User = {
    val actionAttr: Map[String, String] = Map(
      "category" -> action.get("se_category").getOrElse("N/A").asInstanceOf[String],
      "action" -> action.get("se_action").getOrElse("N/A").asInstanceOf[String],
      "label" -> action.get("se_label").getOrElse("N/A").asInstanceOf[String],
      "property" -> action.get("se_property").getOrElse("N/A").asInstanceOf[String],
      "value" -> action.get("se_value").getOrElse("N/A").asInstanceOf[String],
      "tstamp" -> action.get("true_tstamp").getOrElse("N/A").asInstanceOf[String],
      "tzone" -> action.get("os_timezone").getOrElse("N/A").asInstanceOf[String]
    ).transform((key, value) => if(value == null) "N/A" else value).asInstanceOf[Map[String, String]]


    User(id, actions :+ actionAttr)
  }

  def save() = {
    store.save(id, "actions" -> actions)
  }
}

object User {
  val store = new DynamoDB(DynamoDB.userTable)

  def withID(id: String): User = {
    store.find(id) match {
      case Some(user_data) =>
        val attributes = user_data.attributes
        val actions = attributes.find(_.name == "actions") match {
          case Some(actionAttr) =>
            val actionList = actionAttr.value.l.asInstanceOf[Seq[Map[String, String]]]
            Queue[Map[String, String]]() ++ actionList
          case None => Queue()
        }

        User(id, actions)
      case None => User(id)
    }
  }
}
