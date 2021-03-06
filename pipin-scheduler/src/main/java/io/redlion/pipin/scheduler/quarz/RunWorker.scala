package io.redlion.pipin.scheduler.quarz

import io.pipin.core.poll.PollWorker
import io.pipin.core.repository.Project
import io.redlion.pipin.scheduler.zookeeper.ZookeeperFactory
import org.quartz.{JobDetail, JobExecutionContext}
import org.slf4j.LoggerFactory

/**
  * Created by libin on 2020/1/15.
  */
class RunWorker extends org.quartz.Job {
  private val logger = LoggerFactory.getLogger("RunWorker")

  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  def execute (ctx : JobExecutionContext) {


    val jobDetail: JobDetail = ctx.getJobDetail
    val projectId = jobDetail.getJobDataMap.getString("projectId")
    logger.info("execute job for {}", projectId)
    val zookeeperFactory = jobDetail.getJobDataMap.get("zookeeperFactory").asInstanceOf[ZookeeperFactory]
    if(null == zookeeperFactory){
      Project.findById(projectId).map{
        case Some(project) =>
          new PollWorker(project).execute()
      }
    }else{
      zookeeperFactory.connection()
      if (zookeeperFactory.getMonopolyLock){
        logger.info("got monopoly lock, start job for project {}", projectId)
        Project.findById(projectId).map{
          case Some(project) =>
            new PollWorker(project).execute()
        }
      }else{
        logger.info("didn't get monopoly lock")
      }
    }


  }
}
