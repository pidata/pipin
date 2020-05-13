package io.pipin.web.server.projects

import io.pipin.core.domain.Dto
import io.pipin.core.settings.{ConvertSettings, MergeSettings, PollSettings}

/**
  * Created by libin on 2020/5/13.
  */

/*
*
*/
case class ProjectDTO ( _id:String = null,  name:String, pollSettings: PollSettings, convertSettings:ConvertSettings, mergeSettings:MergeSettings) extends Dto
