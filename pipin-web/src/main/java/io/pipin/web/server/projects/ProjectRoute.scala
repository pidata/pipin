package io.pipin.web.server.projects

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import io.pipin.core.poll.PollWorker
import io.pipin.core.repository.Project

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

/**
  * Created by libin on 2020/1/15.
  */
object ProjectRoute {
  def router()(implicit executor: ExecutionContext, materializer:Materializer): Route = {
    pathPrefix( Segment) {
      projectId =>
        pathEnd {
          get {
            onComplete(Project.findById(projectId)) {
              case Success(project) =>
                complete(Project.toDocument(project).toJson())
              case Failure(e) =>
                complete(400, e.getMessage)
            }
          }
        } ~
          pathPrefix("start") {
            pathEnd {
              get {
                onComplete(Project.findById(projectId)) {
                  case Success(project) =>
                    new PollWorker(project).execute()
                    complete(Project.toDocument(project).toJson())
                  case Failure(e) =>
                    complete(400, e.getMessage)
                }
              }
            }
          }
    }
  }
}
