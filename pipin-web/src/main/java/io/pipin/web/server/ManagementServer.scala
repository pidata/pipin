package io.pipin.web.server

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpHeader}
import akka.http.scaladsl.model.headers
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.{ActorMaterializer, Materializer}
import io.pipin.core.PipinSystem
import io.pipin.web.server.jobs.JobRoute
import io.pipin.web.server.projects.ProjectRoute
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext

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

    Http().bindAndHandle(router(), "0.0.0.0", 8989)
    logger.info("management server started")
  }


  private def router()(implicit executor: ExecutionContext, materializer:Materializer): Route = {


    respondWithHeaders(headers.`Content-Type`(ContentTypes.`application/json`), RawHeader("Server", "PiPin")){
      pathPrefix("projects" ) {
        ProjectRoute.router()
      } ~
        pathPrefix("jobs" ) {
          JobRoute.router()
        }
    }
  }
}


object ManagementServer{
  def apply: ManagementServer = new ManagementServer()
}