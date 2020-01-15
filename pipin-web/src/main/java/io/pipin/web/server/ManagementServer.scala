package io.pipin.web.server

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import io.pipin.web.server.jobs.JobRoute
import io.pipin.web.server.projects.ProjectRoute

/**
  * Created by libin on 2020/1/15.
  */

/*
*
*/
class ManagementServer {
  def start(implicit actorSystem: ActorSystem): Unit = {
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = actorSystem.dispatchers.lookup("web-server-dispatcher")
    Http().bindAndHandle(router(), "0.0.0.0", 8080)
  }


  def router(): Route = {
    pathPrefix("projects" ) {
      ProjectRoute.router()
    } ~
      pathPrefix("jobs" ) {
        JobRoute.router()
      }
  }
}


object ManagementServer{
  def apply: ManagementServer = new ManagementServer()
}