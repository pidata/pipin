package io.pipin.web.server.jobs

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import io.pipin.core.domain.Job
import io.pipin.core.repository.{Job, Project}
import org.bson.Document

import scala.concurrent.ExecutionContext
import scala.util.Success

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
                doc.put("results", seq.toList.asJava)
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
        }
    }
  }
}
