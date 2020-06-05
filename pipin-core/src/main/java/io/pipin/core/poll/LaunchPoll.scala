package io.pipin.core.poll

import io.pipin.core.repository.Project

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * Created by libin on 2020/6/5.
  */
object LaunchPoll{

  def main(args:Array[String]): Unit ={
    implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global
    args match {
      case Array(projectId) =>
        val str = Await.result[String](
          Project.findById(projectId).flatMap{
            case Some(project) =>
              new PollWorker(project = project).execute()
          }, Duration(10,"minutes")
        )

        println(str)
        println("done! bye "+ projectId)
        System.exit(0)
      case _ =>
        println("unknown args")
        println(args)

    }

  }

}
