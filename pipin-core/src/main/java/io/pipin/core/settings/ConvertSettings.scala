package io.pipin.core.settings

import java.util

import io.pipin.core.ext.Converter

import scala.collection.JavaConverters._

/**
  * Created by libin on 2020/1/8.
  */
case class ConvertSettings (converter:Converter = (doc: util.Map[String, Object]) => {
  doc.asScala.map {
    case (k, v: util.Map[String, Object]) =>
      (k, v)
  }.asJava
})

