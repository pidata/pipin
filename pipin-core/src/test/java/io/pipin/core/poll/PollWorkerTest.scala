package io.pipin.core.poll

import io.pipin.core.repository.Project
import io.pipin.core.settings.PollSettings

/**
  * Created by libin on 2020/1/9.
  */
object PollWorkerTest {

  def main(args: Array[String]): Unit = {
    val project = Project()
    project.pollSettings = PollSettings("https://mall.lepiepie.com/api/comments?page=0", "page", 1)
    new PollWorker(project).execute()
  }
}
