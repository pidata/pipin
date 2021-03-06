package io.pipin.core.settings

import java.util

import io.pipin.core.ext.EntitySink
import org.slf4j.Logger

import scala.reflect.runtime.{universe => ru}

/**
  * Created by libin on 2020/1/9.
  */
case class MergeSettings (keyMap:Map[String, Array[String]], sinkClass:String = "io.pipin.core.sink.MongoEntitySink"){
  def entitySink(log:Logger):EntitySink = {
    val classMirror = ru.runtimeMirror(getClass.getClassLoader)
    val classTest = classMirror.staticClass(sinkClass)
    val cls1 =  classMirror.reflectClass(classTest)
    val constructor = cls1.reflectConstructor(classTest.primaryConstructor.asMethod)
    constructor.apply(log).asInstanceOf[EntitySink]
  }
}
