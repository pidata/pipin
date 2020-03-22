package io.pipin.core.settings
import java.util

/**
  * Created by libin on 2020/1/8.
  */

case class PollSettings(startUri:String,
                        pageParameter:String,
                        pageStartFrom:Int,
                        method:String = "GET",
                        importer:String = "",
                        traversalClass:String = "io.pipin.core.poll.SimpleTraversal",
                        extraSettings:util.Map[String,String] = new util.HashMap()
)
