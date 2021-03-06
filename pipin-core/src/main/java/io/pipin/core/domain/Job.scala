package io.pipin.core.domain

import akka.stream.{ActorMaterializer, Materializer}
import akka.{Done, NotUsed}
import akka.stream.scaladsl.{Sink, Source, SourceQueueWithComplete}
import org.slf4j.Logger
import org.bson.Document
import io.pipin.core.Converters._
import io.pipin.core.repository.Job

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future, Promise}
/**
  * Created by libin on 2020/1/3.
  */
class Job(val id:String,
          val project:Project,workspace: Workspace, absorbStage:AbsorbStage, convertStage: ConvertStage, mergeStage: MergeStage) {

  var (startTime:Long, duration:Int, status) = (0l,0, 0)
  var errorMessages = ""

  val log: Logger = workspace.getLogger(s"Job")
  def process(source:Source[Document, Any], queueReady:(Any)=>Unit = (_)=>{}, promise:Promise[String] = Promise())(implicit executor: ExecutionContext, materializer:Materializer): Future[String] = {
    log.info("try to process job {}", id)
    status match {
      case 1 =>
        retry(promise)
      case 0 =>
        startTime = System.currentTimeMillis()
        status = 2
        absorbStage.process(source, queueReady).flatMap{
          absorbed =>
            Job.save(this)
            convertStage.process(absorbed)
        }.flatMap{
          converted =>
            Job.save(this)
            mergeStage.process(converted)
        } map {
          case Done =>
            status = 3
            duration =  ((System.currentTimeMillis() - startTime)/1000).toInt
            Job.save(this)
            promise.success("")
        } recover {
          case e:Exception =>
            status = 1
            duration =  ((System.currentTimeMillis() - startTime)/1000).toInt
            errorMessages = e.getMessage
            Job.save(this)
            promise.failure(e)
        }
      case _ =>
        promise.success("")
    }
    promise.future
  }



  private def retry(promise:Promise[String])(implicit executor: ExecutionContext, materializer:Materializer): Unit = {
    startTime = System.currentTimeMillis()
    convertStage.process(absorbStage.fetchDocs).flatMap{
      converted =>
        mergeStage.process(converted)
    } map {
      case Done =>
        status = 3
        duration =  ((System.currentTimeMillis() - startTime)/1000).toInt
        Job.save(this)
        promise.success("")
    } recover {
      case e:Exception =>
        status = 1
        duration =  ((System.currentTimeMillis() - startTime)/1000).toInt
        errorMessages = e.getMessage
        promise.failure(e)
    }
  }

  def toDocument: Document = {
    new Document(Map("id"->id, "startTime"->startTime, "duration"->duration, "status"->status,
      "errorMessages" -> errorMessages,
      "project"->json("_id"->project._id),
      "absorbStage"->absorbStage.toDocument, "convertStage"->convertStage.toDocument, "mergeStage"->mergeStage.toDocument
    ))
  }

}


