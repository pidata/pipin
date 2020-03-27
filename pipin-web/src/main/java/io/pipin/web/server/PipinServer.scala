package io.pipin.web.server

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.{ActorMaterializer, Materializer}
import io.pipin.core.PipinSystem
import io.pipin.core.repository.{Job, Project}
import io.pipin.core.util.UUID
import io.pipin.core.importer.{CSVImporter, JsonImporter}
import io.pipin.core.poll.PollWorker
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext
import scala.util.Success

/**
  * Created by libin on 2020/1/10.
  */

/*
*
*/
class PipinServer {

  private val logger = LoggerFactory.getLogger("ManagementServer")

  def start(): Unit ={
    start(PipinSystem.actorSystem)
  }

  def start(implicit actorSystem: ActorSystem): Unit ={
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = actorSystem.dispatchers.lookup("web-server-dispatcher")
    Http().bindAndHandle(router(), "0.0.0.0", 8080)
    logger.info("pipin server started")
  }


  def router()(implicit executor: ExecutionContext, materializer:Materializer): Route ={
    pathPrefix("projects" / Segment){
      projectId =>
        pathPrefix("endpoints"){
            pathEnd{
              post{
                fileUpload("csv"){
                  case (fileInfo, source) =>
                    onSuccess(Project.findById(projectId)){
                      case Some(project) =>

                        Job(UUID(),project).process(new CSVImporter().stream(source))
                        complete("importing started")
                    }
                } ~ fileUpload("json"){
                  case (fileInfo, source) =>
                    onSuccess(Project.findById(projectId)){
                      case Some(project) =>

                        Job(UUID(),project).process(new JsonImporter().stream(source))
                        complete("importing started")
                    }
                }

              }
            }
        }
    }


  }

}


object PipinServer{
  def apply: PipinServer = new PipinServer()

  def main(args: Array[String]): Unit = {
    val pipinServer = new PipinServer()
    pipinServer.start(PipinSystem.actorSystem)
  }
}