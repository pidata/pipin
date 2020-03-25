package io.pipin.core.domain

import java.util

import io.pipin.core.settings.{ConvertSettings, MergeSettings, PollSettings}
import io.pipin.core.sink.MongoEntitySink

/**
  * Created by libin on 2020/1/3.
  */
class Project(val _id:String, var name:String, var source:String = "poll", keys:Array[String] = Array() ) {
  private val entity = name.replace(" ","_")
  var convertSettings:ConvertSettings = ConvertSettings(entity)
  var mergeSettings:MergeSettings = MergeSettings(Map(entity -> keys), "io.pipin.core.sink.MongoEntitySink")
  var pollSettings: PollSettings = PollSettings("", "", 0, "")
  val workspace = Workspace(id = _id)
  var jobTrigger:JobTrigger = _
}
