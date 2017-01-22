package com.crystal
package models

// Scala
import scala.collection.immutable.Queue
import scala.collection.JavaConversions._

// Persistent Storage
import stores.DynamoDB

case class User(val id: String, persistedActions: Queue[UserAction] = Queue(), newActions: Queue[UserAction] = Queue()) {
  import User._

  val actions = persistedActions ++ newActions

  def performedAction(action: Map[String, Any]): User = {
    val newAction = UserAction(
      userID = id,
      category = action.get("se_category").getOrElse("N/A").asInstanceOf[String],
      action = action.get("se_action").getOrElse("N/A").asInstanceOf[String],
      label = action.get("se_label").getOrElse("N/A").asInstanceOf[String],
      property = action.get("se_property").getOrElse("N/A").asInstanceOf[String],
      value = action.get("se_value").getOrElse("N/A").asInstanceOf[String],
      tstamp = action.get("collector_tstamp").get.asInstanceOf[String]
    )

    User(id, persistedActions, newActions :+ newAction)
  }

  def save() = {
    newActions.map { action => action.save() }
  }

  def toMap(): Map[String, Any] = {
    Map(
      "id" -> id,
      "actions" -> actions.map(_.toMap)
    )
  }
}

object User {
  def withID(id: String): User = {
    if (id == null || id.isEmpty) {
      return User("")
    }

    User(id, Queue[UserAction]() ++ UserAction.allFor(id))
  }
}
