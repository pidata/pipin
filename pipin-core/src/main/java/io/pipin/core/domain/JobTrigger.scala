package io.pipin.core.domain

/**
  * Created by libin on 2020/1/8.
  */
case class JobTrigger (cron:String, enable:Boolean = true, priority:Int = 0, misfireInstruction:Int = 0)
