package io.pipin.core.poll

import io.pipin.core.repository.Project
import io.pipin.core.util.UUID
import io.redlion.pipin.scheduler.PollWorker

/**
  * Created by libin on 2020/1/9.
  */
object PollWorkerTest {

  def main(args: Array[String]): Unit = {
    val project = Project()
    new PollWorker(project).execute()
  }
}
