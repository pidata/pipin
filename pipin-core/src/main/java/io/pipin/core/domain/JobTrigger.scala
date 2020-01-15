package io.pipin.core.domain

/**
  * Created by libin on 2020/1/8.
  */
case class JobTrigger (key:String, cron:String, priority:Int = 0, misfireInstruction:Int = 0)
