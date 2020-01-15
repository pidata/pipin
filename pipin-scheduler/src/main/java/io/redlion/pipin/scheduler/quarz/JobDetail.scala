package io.redlion.pipin.scheduler.quarz

import io.pipin.core.domain.Job
import io.redlion.pipin.scheduler.zookeeper.ZookeeperFactory
import org.quartz.{JobBuilder, JobDataMap, JobDetail}

/**
  * Created by libin on 2020/1/15.
  */
object JobDetail{
  def apply(job:Job): JobDetail = {
    val jobDataMap = new JobDataMap()
    jobDataMap.put("projectId", job.project.id)
    jobDataMap.put("ZookeeperFactory", ZookeeperFactory(job.project.id))
    JobBuilder.newJob(classOf[RunWorker])
      .withIdentity(job.id)
      .setJobData(jobDataMap)
      .build()
  }
}
