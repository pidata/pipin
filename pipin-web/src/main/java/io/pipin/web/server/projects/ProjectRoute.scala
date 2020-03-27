package io.pipin.web.server.projects

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import io.pipin.core.poll.PollWorker
import io.pipin.core.repository.{Job, Project}
import io.pipin.core.util.UUID
import org.bson.Document

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}
import scala.collection.JavaConverters._

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
                      doc.put("results",  seq.toList.asJava)
                      complete(doc.toJson())
                  }
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
                doc.put("results",  seq.map(Project.toDocument).toList.asJava)
                complete(doc.toJson())
            }
        }
      } ~
      post{
        entity(as[String]){
          body =>
            val doc = Document.parse(body)
            val project = if(doc.containsKey("_id")){
              Project( Document.parse(body))
            }else{
              Project(UUID(), Document.parse(body))
            }
            onComplete(Project.save(project)){
              case Success(Some(doc)) =>
                complete(doc.toJson)
              case Success(None) =>
                complete(doc.toJson)
            }
        }
      }
    }
  }
}
