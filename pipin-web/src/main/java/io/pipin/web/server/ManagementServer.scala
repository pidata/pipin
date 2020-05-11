package io.pipin.web.server

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.coding.Gzip
import akka.http.scaladsl.model.{ContentTypes, HttpHeader}
import akka.http.scaladsl.model.headers
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.{ActorMaterializer, Materializer}
import io.pipin.core.PipinSystem
import io.pipin.core.repository.{Job, Stage}
import io.pipin.web.server.jobs.JobRoute
import io.pipin.web.server.projects.ProjectRoute
import org.bson.Document
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext
import scala.util.Success
import scala.collection.JavaConverters._

/**
  * Created by libin on 2020/1/15.
  */

/*
*
*/
class ManagementServer {

  private val logger = LoggerFactory.getLogger("ManagementServer")
  def start(): Unit ={
    start(PipinSystem.actorSystem)
  }

  def start(implicit actorSystem: ActorSystem): Unit = {
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = actorSystem.dispatchers.lookup("web-server-dispatcher")

    Http().bindAndHandle(router(), "0.0.0.0", 8787)
    logger.info("management server started")
  }


  private def router()(implicit executor: ExecutionContext, materializer:Materializer): Route = {


    respondWithHeaders(headers.`Content-Type`(ContentTypes.`application/json`), RawHeader("Server", "PiPin")){
      pathPrefix("api"){
        pathPrefix("projects" ) {
          ProjectRoute.router()
        } ~
          pathPrefix("jobs" ) {
            JobRoute.router()
          } ~
          pathPrefix("stages"){
            path(Segment){
              stageId =>
                get{
                  parameters('page ? 0){
                    page =>
                      onComplete(Stage.findStageById(stageId, page)){
                        case Success(seq) =>
                          val doc = new Document()
                          doc.put("content",  seq.toList.asJava)
                          complete(doc.toJson())
                      }
                  }
                }
            }
          }
      } ~
        pathPrefix("web"){
          respondWithHeaders(headers.`Content-Type`(ContentTypes.`text/html(UTF-8)`)){
            encodeResponseWith(Gzip){
              getFromResourceDirectory("web")
            }
          }
        }
    }
  }
}


object ManagementServer{
  def apply: ManagementServer = new ManagementServer()
}