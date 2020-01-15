package io.redlion.pipin.scheduler.quarz

import io.pipin.core.domain.JobTrigger
import org.quartz.{CronScheduleBuilder, CronTrigger, TriggerBuilder}

/**
  * Created by libin on 2020/1/8.
  */

object Trigger{

  def apply(jobTrigger: JobTrigger): CronTrigger = {
    val (key, expression, priority) = (jobTrigger.key, jobTrigger.cron, jobTrigger.priority)

    val cronScheduleBuilder = CronScheduleBuilder.cronSchedule(expression)
    TriggerBuilder.newTrigger
            .withIdentity(key)
            .withSchedule(cronScheduleBuilder)
            .withPriority(priority).build()
  }

}




