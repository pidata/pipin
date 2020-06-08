package io.pipin.core.poll

import java.nio.file.Paths

import akka.stream.{ActorMaterializer, Materializer}
import akka.stream.scaladsl.FileIO
import io.pipin.core.PipinSystem
import io.pipin.core.importer.CSVImporter
import io.pipin.core.repository.{Job, Project}
import io.pipin.core.util.UUID

import scala.concurrent.{Await, Promise}
import scala.concurrent.duration.Duration
import scala.io.Source

/**
  * Created by libin on 2020/6/5.
  */
object Launch{

  def main(args:Array[String]): Unit ={
    implicit val actorSystem = PipinSystem.actorSystem
    implicit val materialize: Materializer = ActorMaterializer()
    implicit val executionContext = actorSystem.dispatchers.lookup("poll-dispatcher")
    args match {
      case Array(projectId, action, target) =>
        val str = Await.result[String](
          Project.findById(projectId).flatMap{
            case Some(project) =>
              (action , target) match {
                case ("poll", _) =>
                  new PollWorker(project = project).execute()
                case ("import", file) =>
                  if(file.endsWith("csv")){
                    Job(UUID(),project).process(new CSVImporter().stream(FileIO.fromPath(Paths.get(file))))
                  }else if(file.endsWith("json")){
                    Job(UUID(),project).process(new CSVImporter().stream(FileIO.fromPath(Paths.get(file))))
                  }else{
                    throw new Exception("unknown import file type (csv/json) for file: " + file)
                  }
              }
            case None =>
              throw new Exception("project not found " + projectId)
          }.recover{
            case e:Throwable =>
              println("failed: " + e.getMessage)
            ""
          }, Duration(10,"minutes")
        )

        println(str)
        println("done! bye "+ projectId)
        System.exit(0)
      case _ =>
        println("unknown args")
        println(args)
        System.exit(0)

    }

  }

}
