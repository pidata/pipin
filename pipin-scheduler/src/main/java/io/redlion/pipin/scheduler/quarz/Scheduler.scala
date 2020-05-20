package io.redlion.pipin.scheduler.quarz

import io.pipin.core.domain.Project
import io.pipin.core.repository
import io.pipin.core.repository.Job
import io.pipin.core.util.UUID
import org.quartz.impl.StdSchedulerFactory
import org.slf4j.LoggerFactory

import scala.concurrent.Future

/**
  * Created by libin on 2020/1/15.
  */

/*
*
*/
class Scheduler {

  private val logger = LoggerFactory.getLogger("Scheduler")
  val scheduler = StdSchedulerFactory.getDefaultScheduler
  def scheduleJob(project: Project): Boolean = {
    if(project.jobTrigger.enable){
      logger.info(s"schedule job for ${project._id} with trigger ${project.jobTrigger}")
      scheduler.scheduleJob(JobDetail(Job(UUID(), project)), Trigger(project.jobTrigger, project._id))
    }
    true
  }

  def scheduleAll(): Future[Seq[Boolean]] = {
    implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global
    repository.Project.findAllWithCron().map(projects =>
      projects.map(scheduleJob)).recover{
      case e:Exception =>
        logger.error("schedule job failed",e)
        Seq(false)
    }
  }

  def start(): Unit ={
    scheduler.start()
  }

}
