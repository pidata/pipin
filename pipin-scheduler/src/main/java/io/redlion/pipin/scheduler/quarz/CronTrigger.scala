package io.redlion.pipin.scheduler.quarz

import java.util.Date

import io.pipin.core.domain.JobTrigger
import org.quartz._

/**
  * Created by libin on 2020/1/8.
  */

object Trigger{

  def apply(jobTrigger: JobTrigger): Trigger = {
    val (key, expression, priority) = (jobTrigger.key, jobTrigger.cron, jobTrigger.priority)

    val cronScheduleBuilder = CronScheduleBuilder.cronSchedule(expression)
    TriggerBuilder.newTrigger
            .withIdentity(key)
            .withSchedule(cronScheduleBuilder)
            .withPriority(priority).build()
//
//    val startTime = new Date()
//    startTime.setTime(startTime.getTime + 3000)
//    val endTime = new Date()
//    endTime.setTime(endTime.getTime + 6000)
//    TriggerBuilder.newTrigger.withIdentity("myTrigger", "group1").startAt(startTime)
//    .endAt(endTime).withSchedule(SimpleScheduleBuilder.simpleSchedule.withIntervalInSeconds(2).repeatForever).build()
  }

}




