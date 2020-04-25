package io.pipin.core.ext

import java.util

import io.pipin.core.util.Hashing
import org.slf4j.Logger

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

class KeyRuleInMap(rules:util.Map[String, Array[String]], log:Logger) extends KeyRule{
  override def keyFields(entity: String): Option[util.Collection[String]] = {
    Option(rules.get(entity)) map {
      rule =>
        util.Arrays.asList(rule:_*)
    } orElse{
      log.error("can not find rule for {}, in {}, will use all fields to generate key", entity, rules)
      None
    }
  }
}