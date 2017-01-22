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
}

object UserAction {
  val store = new DynamoDB(DynamoDB.userActionTable)

  def allFor(userID: String): Seq[UserAction] = {
    store.findAll(userID).map { item =>
      val attribs = item.attributes

      UserAction(
        userID = attribs.find(_.name == "user_id").get.value.s.get,
        category = attribs.find(_.name == "category").get.value.s.get,
        action = attribs.find(_.name == "action").get.value.s.get,
        label = attribs.find(_.name == "label").get.value.s.get,
        property = attribs.find(_.name == "property").get.value.s.get,
        value = attribs.find(_.name == "value").get.value.s.get,
        tstamp = attribs.find(_.name == "tstamp").get.value.s.get
      )
    }
  }
}
