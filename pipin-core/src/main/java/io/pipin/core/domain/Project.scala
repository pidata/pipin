package io.pipin.core.domain

import java.util

import io.pipin.core.ext.EntitySink
import io.pipin.core.settings.{ConvertSettings, MergeSettings, PollSettings}
import io.pipin.core.util.UUID

/**
  * Created by libin on 2020/1/3.
  */
class Project(val id:String, var name:String, var source:String = "poll") {

  var convertSettings:ConvertSettings = ConvertSettings()
  var mergeSettings:MergeSettings = MergeSettings(new util.HashMap[String, util.List[String]], new EntitySink {})
  var pollSettings: PollSettings = PollSettings("", "", 0, "")
  val workspace = Workspace(id = id)
  var jobTrigger:JobTrigger = _
}
