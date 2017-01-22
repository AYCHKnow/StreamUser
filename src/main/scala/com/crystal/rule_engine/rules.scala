package com.crystal
package rule_engine
package rules

// User objects
import models.User

abstract class Rule {
  val operator: String
  val property: Option[String]
  val ruleset: Seq[Rule]
  val value: Option[Any]

  def fulfilledBy(obj: Map[String, Any]): Boolean = false
}

case class AndRule(val operator: String = Operator.And, val property: Option[String] = None,
                   val ruleset: Seq[Rule] = Vector(), val value: Option[Any] = None) extends Rule {

  override def fulfilledBy(obj: Map[String, Any]): Boolean = {
    ruleset.foldLeft(true) { (fulfilled, rule) => fulfilled && rule.fulfilledBy(obj) }
  }
}

case class OrRule(val operator: String = Operator.Or, val property: Option[String] = None,
                  val ruleset: Seq[Rule] = Vector(), val value: Option[Any] = None) extends Rule {

  override def fulfilledBy(obj: Map[String, Any]): Boolean = {
    ruleset.foldLeft(true) { (fulfilled, rule) => fulfilled || rule.fulfilledBy(obj) }
  }
}

case class NotRule(val operator: String = Operator.Not, val property: Option[String] = None,
                   val ruleset: Seq[Rule] = Vector(), val value: Option[Any] = None) extends Rule {

  override def fulfilledBy(obj: Map[String, Any]): Boolean = {
    ruleset.foldLeft(true) { (fulfilled, rule) => fulfilled && !rule.fulfilledBy(obj) }
  }
}

case class EqualsRule(val operator: String = Operator.Equals, val property: Option[String] = None,
                      val ruleset: Seq[Rule] = Vector(), val value: Option[Any] = None) extends Rule {

  override def fulfilledBy(obj: Map[String, Any]): Boolean = {
    obj(property.get) == value.get
  }
}

case class LessThanRule(val operator: String = Operator.LessThan, val property: Option[String] = None,
                        val ruleset: Seq[Rule] = Vector(), val value: Option[Any] = None) extends Rule {

  override def fulfilledBy(obj: Map[String, Any]): Boolean = {
    obj(property.get) match {
      case propVal: String => propVal < value.get.asInstanceOf[String]
      case propVal: Int => propVal < value.get.asInstanceOf[Int]
      case _ => false
    }
  }
}

case class GreaterThanRule(val operator: String = Operator.GreaterThan, val property: Option[String] = None,
                           val ruleset: Seq[Rule] = Vector(), val value: Option[Any] = None) extends Rule {

  override def fulfilledBy(obj: Map[String, Any]): Boolean = {
    obj(property.get) match {
      case propVal: String => propVal > value.get.asInstanceOf[String]
      case propVal: Int => propVal > value.get.asInstanceOf[Int]
      case _ => false
    }
  }
}

case class ContainsAllRule(val operator: String = Operator.ContainsAll, val property: Option[String] = None,
                           val ruleset: Seq[Rule] = Vector(), val value: Option[Any] = None) extends Rule {

  override def fulfilledBy(obj: Map[String, Any]): Boolean = {
    ruleset.foldLeft(true) { (allFulfilled, rule) =>
      val objs = obj(property.get).asInstanceOf[Seq[Map[String, Any]]]

      allFulfilled && objs.foldLeft(false) { (currFulfilled, obj) => currFulfilled || rule.fulfilledBy(obj) }
    }
  }
}

case class ContainsNoneRule(val operator: String = Operator.ContainsNone, val property: Option[String] = None,
                            val ruleset: Seq[Rule] = Vector(), val value: Option[Any] = None) extends Rule {

  override def fulfilledBy(obj: Map[String, Any]): Boolean = {
    ruleset.foldLeft(true) { (noneFulfilled, rule) =>
      val objs = obj(property.get).asInstanceOf[Seq[Map[String, Any]]]

      noneFulfilled && !objs.foldLeft(false) { (currFulfilled, obj) => currFulfilled || rule.fulfilledBy(obj) }
    }
  }
}
