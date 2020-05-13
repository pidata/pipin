package io.pipin.web.server.projects

import akka.http.scaladsl.coding.Gzip
import akka.http.scaladsl.model.{ContentTypes, headers}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer

import io.pipin.core.poll.PollWorker
import io.pipin.core.repository.{Job, Project}
import io.pipin.core.util.UUID
import io.pipin.web.server.RestJsonSupport
import org.bson.Document

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}
import scala.collection.JavaConverters._

/**
  * Created by libin on 2020/1/15.
  */
object ProjectRoute extends RestJsonSupport{
  def router()(implicit executor: ExecutionContext, materializer:Materializer): Route = {
    pathPrefix( Segment) {
      projectId =>
        pathEnd {
          get {
            onComplete(Project.findById(projectId)) {
              case Success(Some(project)) =>
                complete(Project.toDocument(project).toJson())
              case Success(None) =>
                complete(404,"")
              case Failure(e) =>
                complete(400, e.getMessage)
            }
          }
        } ~
          pathPrefix("start") {
            pathEnd {
              get {
                onComplete(Project.findById(projectId)) {
                  case Success(Some(project)) =>
                    new PollWorker(project).execute()
                    complete(Project.toDocument(project).toJson())
                  case Success(None) =>
                    complete(404,"")
                  case Failure(e) =>
                    complete(400, e.getMessage)
                }
              }
            }
          } ~
          path("jobs"){
            get{
              parameters('page ? 0){
                page =>
                  onComplete(Job.findByProject(projectId, page)){
                    case Success(seq) =>
                      val doc = new Document()
                      doc.put("content",  seq.toList.asJava)
                      complete(doc.toJson())
                  }
              }
            }
          } ~
          pathPrefix("logs"){
            respondWithHeaders(headers.`Content-Type`(ContentTypes.`text/html(UTF-8)`)){
              encodeResponseWith(Gzip) {
                getFromBrowseableDirectories(s"workspaces/$projectId/logs")
              }
            }
          }
    } ~
    pathEnd {
      get{
        parameters('page ? 0){
          page =>
            onComplete(Project.findAll(page)){
              case Success(seq) =>
                val doc = new Document()
                doc.put("content",  seq.map(Project.toDocument).toList.asJava)
                complete(doc.toJson())
            }
        }
      } ~
      post{
        entity(as[ProjectDTO]){
          dto =>

            Option(dto._id) match {
              case Some(id) =>
                onComplete(Project.findById(id).flatMap{
                  case Some(project) =>
                    fillProject(project, dto)
                    Project.save(project).map(_=>Some(project))
                  case None =>
                    Future(None)
                }){
                  case Success(Some(project)) =>
                    complete(project)
                  case Success(None) =>
                    complete(404, "")
                  case _ =>
                    complete(500, "")
                }
              case None =>
                val project = new io.pipin.core.domain.Project(UUID(),dto.name)
                fillProject(project, dto)
                onComplete(Project.save(project)){
                  case Success(_) =>
                    complete(project)
                  case _ =>
                    complete(500, "")
                }
            }
        }
      }
    }
  }

  def fillProject(project: io.pipin.core.domain.Project, projectDTO: ProjectDTO): Unit ={
    project.name = projectDTO.name
    project.pollSettings = projectDTO.pollSettings
    project.convertSettings = projectDTO.convertSettings
    project.mergeSettings = projectDTO.mergeSettings
  }
}
