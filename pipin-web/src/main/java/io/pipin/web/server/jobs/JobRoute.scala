package io.pipin.web.server.jobs

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import io.pipin.core.domain.Job
import io.pipin.core.repository.{Job, Project}
import org.bson.Document

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}
import scala.collection.JavaConverters._

/**
  * Created by libin on 2020/1/15.
  */
object JobRoute {
  def router()(implicit executor: ExecutionContext, materializer:Materializer): Route = {
    pathEnd {
      get{
        parameters('page ? 0){
          page =>
            onComplete(Job.findAll(page)){
              case Success(seq: Seq[Document]) =>
                val doc = new Document()
                doc.put("content", seq.toList.asJava)
                complete(doc.toJson())
            }
        }
      }
    } ~
    pathPrefix(Segment){
      jobId =>
        pathEnd{
          get{
            onComplete(Job.findById(jobId)){
              case Success(Some(doc)) =>
                complete(doc.toJson)
              case Success(None) =>
                complete(404,"")
            }
          }
        } ~
        path("start"){
          get{
            onComplete(Job.findById(jobId).flatMap(x=>Job.applyFromDoc(x.get))){
              case Success(Some(job)) =>
                extractActorSystem{
                  implicit actorSystem =>
                    val project = job.project
                    val traversal = project.traversal
                    job.process(traversal.stream(), traversal.start)
                    complete(job.toDocument.toJson())
                }
              case Failure(e) =>

                complete(404,e.getMessage)
            }
          }
        }
    }
  }
}
