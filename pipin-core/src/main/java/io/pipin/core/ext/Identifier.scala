package io.pipin.core.ext

import java.util

import io.pipin.core.util.Hashing

import scala.collection.JavaConverters._

/**
  * Created by libin on 2020/1/5.
  */
class Identifier(keyRule: KeyRule) {
  def genKey(entity:String, obj:util.Map[String, Object]):Entity = {
    val str = keyRule.keyFields(entity).asScala.map{
      field =>
        obj.get(field).toString
    }.reduce(_+_)
    Entity(entity, Hashing.fnvHashStr(str), obj)
  }
}

trait KeyRule{
  def keyFields(entity:String):util.List[String]
}

class KeyRuleInMap(rule:util.Map[String, util.List[String]]) extends KeyRule{
  override def keyFields(entity: String): util.List[String] = {
    rule.get(entity)
  }
}