package io.pipin.core.domain

import akka.stream.{ActorMaterializer, Materializer}
import akka.{Done, NotUsed}
import akka.stream.scaladsl.{Sink, Source, SourceQueueWithComplete}
import org.slf4j.Logger
import org.bson.Document
import io.pipin.core.Converters._
import io.pipin.core.repository.Job
import io.pipin.core.settings.{ConvertSettings, MergeSettings}

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext
/**
  * Created by libin on 2020/1/3.
  */
class Job(val id:String,
          val project:Project,workspace: Workspace, absorbStage:AbsorbStage, convertStage: ConvertStage, mergeStage: MergeStage) {

  var (startTime:Long, duration:Int, status) = (0l,0, 0)
  var errorMessages = ""

  val log: Logger = workspace.getLogger(s"Job")
  def process(source:Source[Document, Any], queueReady:(Any)=>Unit = (_)=>{})(implicit executor: ExecutionContext, materializer:Materializer): Unit ={
    status match {
      case 1 =>
        retry()
      case 2 =>
      case 3 =>
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
            Done
        } recover {
          case e:Exception =>
            status = 1
            errorMessages = e.getMessage
            Job.save(this)
        }
      case _ =>
    }
  }



  def retry()(implicit executor: ExecutionContext, materializer:Materializer): Unit = {
    convertStage.process(absorbStage.fetchDocs).flatMap{
      converted =>
        mergeStage.process(converted)
    } map {
      case Done =>
        status = 3
        duration =  ((System.currentTimeMillis() - startTime)/1000).toInt
        Done
    } recover {
      case e:Exception =>
        status = 1
        errorMessages = e.getMessage
    }
  }

  def toDocument: Document = {
    new Document(Map("id"->id, "startTime"->startTime, "duration"->duration, "status"->status,
      "absorbStage"->absorbStage.toDocument, "convertStage"->convertStage.toDocument, "mergeStage"->mergeStage.toDocument
    ))
  }

}


