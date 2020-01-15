package io.pipin.web.server

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import io.pipin.core.PipinSystem
import io.pipin.core.repository.{Job, Project}
import io.pipin.core.util.UUID
import io.pipin.core.importer.{CSVImporter, JsonImporter}

/**
  * Created by libin on 2020/1/10.
  */

/*
*
*/
class PipinServer {

  def start(implicit actorSystem: ActorSystem): Unit ={
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = actorSystem.dispatchers.lookup("web-server-dispatcher")
    Http().bindAndHandle(router(), "0.0.0.0", 8080)
  }


  def router(): Route ={
    pathPrefix("endpoints" / Segment){
      projectId =>
        pathEnd{
          post{
            fileUpload("csv"){
              case (fileInfo, source) =>
                onSuccess(Project.findById(projectId)){
                  project =>

                    Job(UUID(),project).process(new CSVImporter().stream(source))
                    complete("importing started")
                }
            } ~ fileUpload("json"){
              case (fileInfo, source) =>
                onSuccess(Project.findById(projectId)){
                  project =>

                    Job(UUID(),project).process(new JsonImporter().stream(source))
                    complete("importing started")
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