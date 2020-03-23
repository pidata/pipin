package io.pipin.core.settings

import java.util

import io.pipin.core.ext.Converter

import scala.collection.JavaConverters._
import io.pipin.core.Converters._

import scala.reflect.runtime.{universe => ru}

/**
  * Created by libin on 2020/1/8.
  */
case class ConvertSettings (defaultEntity:String, filter:Array[String] = Array.empty, converterClass:String = "io.pipin.core.convert.FlatAndFilterConverter") {
  def converter:Converter = {
    val classMirror = ru.runtimeMirror(getClass.getClassLoader)
    val classTest = classMirror.staticClass(converterClass)
    val cls1 =  classMirror.reflectClass(classTest)
    val constructor = cls1.reflectConstructor(classTest.primaryConstructor.asMethod)
    constructor.apply(defaultEntity, filter).asInstanceOf[Converter]
  }
}

