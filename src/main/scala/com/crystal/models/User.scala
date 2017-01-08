package com.crystal
package models

// Persistent Storage
import stores.DynamoDB
import awscala.dynamodbv2.AttributeType

case class User(val id: String, last_action: Option[String] = None) {
  import User._

  def performedAction(action: String): User = {
    User(id, Some(action))
  }

  def save() = {
    store.save(id, "last_action" -> last_action.getOrElse(None))
  }
}

object User {
  val store = new DynamoDB(DynamoDB.userTable)

  def withID(id: String): User = {
    store.find(id) match {
      case Some(user_data) =>
        val attributes = user_data.attributes
        val lastAction = attributes.find(_.name == "last_action") match {
          case Some(actionAttr) => actionAttr.value.s
          case None => None
        }

        User(id, lastAction)
      case None => User(id)
    }
  }
}
