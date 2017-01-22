package com.crystal
package models

// Persistent Storage
import stores.DynamoDB

case class UserAction(userID: String, category: String, action: String, label: String,
                      property: String, value: String, tstamp: String) {
  import UserAction._

  def save() = {
    store.save(
      userID,
      "category" -> category,
      "action" -> action,
      "label" -> label,
      "property" -> property,
      "value" -> value,
      "tstamp" -> tstamp
    )
  }

  def toMap(): Map[String, Any] = {
    Map(
      "user_id" -> userID,
      "category" -> category,
      "action" -> action,
      "label" -> label,
      "property" -> property,
      "value" -> value,
      "tstamp" -> tstamp
    )
  }
}

object UserAction {
  val store = new DynamoDB(DynamoDB.userActionTable)

  def allFor(userID: String): Seq[UserAction] = {
    store.findAll(userID).map { item =>
      val attribs = item.attributes

      UserAction(
        userID = attribs.find(_.name == "user_id").get.value.s.get,
        category = attribs.find(_.name == "category").flatMap(_.value.s).getOrElse(""),
        action = attribs.find(_.name == "action").flatMap(_.value.s).getOrElse(""),
        label = attribs.find(_.name == "label").flatMap(_.value.s).getOrElse(""),
        property = attribs.find(_.name == "property").flatMap(_.value.s).getOrElse(""),
        value = attribs.find(_.name == "value").flatMap(_.value.s).getOrElse(""),
        tstamp = attribs.find(_.name == "tstamp").get.value.s.get
      )
    }
  }
}
