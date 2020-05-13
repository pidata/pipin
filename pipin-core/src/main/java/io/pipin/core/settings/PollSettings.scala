package io.pipin.core.settings
import java.util

import scala.collection.mutable

import scala.collection.JavaConverters._

/**
  * Created by libin on 2020/1/8.
  */

case class PollSettings(startUri:String,
                        pageParameter:String = "page",
                        pageStartFrom:Int = 0,
                        method:String = "GET",
                        importer:String = "",
                        traversalClass:String = "io.pipin.core.poll.SimpleTraversal",
                        extraParams:mutable.Map[String,String] = mutable.Map()
){
  def extraSettings(): util.Map[String, String] = {
    extraParams.asJava
  }
}
