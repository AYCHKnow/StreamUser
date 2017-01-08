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
    store.save(id, "last_action" -> last_action)
  }
}

object User {
  val store = new DynamoDB(DynamoDB.userTable)

  def withID(id: String): User = {
    store.find(id) match {
      case Some(user_data) => User(id)
      case None => User(id)
    }
  }
}
