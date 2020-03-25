package io.pipin.core.ext

import java.util

import io.pipin.core.util.Hashing

import scala.collection.JavaConverters._

/**
  * Created by libin on 2020/1/5.
  */
class Identifier(keyRule: KeyRule) {
  def genKey(entity:String, obj:util.Map[String, Object]):Entity = {
    val str = keyRule.keyFields(entity).orElse(Some(obj.keySet())).map {
      fields =>
        fields.asScala.map {
          field =>
            field + obj.get(field)
        }.reduce(_ + _)
    }.get
    Entity(entity, Hashing.fnvHashStr(str), obj)
  }
}

trait KeyRule{
  def keyFields(entity:String):Option[util.Collection[String]]
}

class KeyRuleInMap(rule:util.Map[String, Array[String]]) extends KeyRule{
  override def keyFields(entity: String): Option[util.Collection[String]] = {
    Option(util.Arrays.asList(rule.get(entity):_*) )
  }
}