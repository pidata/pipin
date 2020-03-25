package io.pipin.core.settings

import java.util

import io.pipin.core.ext.EntitySink


import scala.reflect.runtime.{universe => ru}

/**
  * Created by libin on 2020/1/9.
  */
case class MergeSettings (keyMap:Map[String, Array[String]], sinkClass:String){
  def entitySink:EntitySink = {
    val classMirror = ru.runtimeMirror(getClass.getClassLoader)
    val classTest = classMirror.staticClass(sinkClass)
    val cls1 =  classMirror.reflectClass(classTest)
    val constructor = cls1.reflectConstructor(classTest.primaryConstructor.asMethod)
    constructor.apply().asInstanceOf[EntitySink]
  }
}
