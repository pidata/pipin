package io.redlion.pipin.scheduler.quarz

import io.pipin.core.domain.Project
import io.pipin.core.repository.Job
import io.pipin.core.util.UUID
import org.quartz.impl.StdSchedulerFactory

/**
  * Created by libin on 2020/1/15.
  */

/*
*
*/
class Scheduler {


  val schedulerFactory = new StdSchedulerFactory
  val scheduler = schedulerFactory.getScheduler
  def scheduleJob(project: Project): Unit ={
    scheduler.scheduleJob(JobDetail(Job(UUID(), project)), Trigger(project.jobTrigger))
  }

}
