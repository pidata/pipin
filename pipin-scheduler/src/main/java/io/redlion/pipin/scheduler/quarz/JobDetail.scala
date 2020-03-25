package io.redlion.pipin.scheduler.quarz

import io.pipin.core.domain.Job
import io.redlion.pipin.scheduler.zookeeper.ZookeeperFactory
import org.quartz.{JobBuilder, JobDataMap, JobDetail}
import org.slf4j.LoggerFactory

/**
  * Created by libin on 2020/1/15.
  */
object JobDetail{
  private val logger = LoggerFactory.getLogger("JobDetail")
  def apply(job:Job): JobDetail = {
    val jobDataMap = new JobDataMap()
    jobDataMap.put("projectId", job.project._id)

    try{
      jobDataMap.put("ZookeeperFactory", ZookeeperFactory(job.project._id))
    }catch {
      case e:Throwable =>
        logger.error("zookeeper setting failed", e)
    }



    JobBuilder.newJob(classOf[RunWorker])
      .withIdentity(job.id)
      .setJobData(jobDataMap)
      .build()
  }
}
