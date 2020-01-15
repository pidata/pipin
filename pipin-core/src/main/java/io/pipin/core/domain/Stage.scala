package io.pipin.core.domain

import java.util

import akka.stream.Materializer
import akka.{Done, NotUsed}
import akka.stream.alpakka.mongodb.scaladsl.MongoSource
import akka.stream.scaladsl.{Sink, Source}
import org.slf4j.Logger
import io.pipin.core.exception.StageException

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success, Try}
import com.mongodb.reactivestreams.client.{FindPublisher, MongoCollection}
import org.bson.Document
import io.pipin.core.ext._
import io.pipin.core.Converters._

import scala.collection.JavaConverters._

/**
  * Created by libin on 2020/1/3.
  */


trait Stage {
  val id:String
  val phase:String

  val log:Logger

  val updateTime:Long = System.currentTimeMillis()

  var result: Try[String] = Success("")
  var status:Int = 0

  def failed(exception: Exception){
    status = 1
    result = Failure(exception)
  }

  def ok(str: String){
    status = 3
    result = Success(str)
  }

  def toDocument: Document = {
    new Document(Map("id"->id, "phase"->phase, "status"->status, "updateTime"->updateTime, "result" -> (result match {
      case Success(str) =>
        new Document("result","success")
      case Failure(e) =>
        new Document(Map("result"->"failed", "error"->e.getMessage))
    })))
  }
}


class AbsorbStage(override val id:String, mongoCollection: MongoCollection[Document])(override implicit val log:Logger) extends Stage {

  def absorb(doc:Document)(implicit executor: ExecutionContext): Future[Document] = {

    //("id"->id, "updateTime"->updateTime)

    doc.asInstanceOf[util.Map[String,Object]] putAll json("stageInfo" -> json("id" -> id, "updateTime" -> updateTime))
    mongoCollection.insertOne(doc).asFuture.map(_ =>doc)
  }

  def process(source:Source[Document, Any])(implicit executor: ExecutionContext, materializer:Materializer): Future[Source[Document, Any]] ={

    status match {
      case 3 =>
        Future{
          fetchDocs
        }
      case 0|1 =>
        status = 2
        source.mapAsync(10){
          doc =>
            absorb(doc)
        }.runWith(Sink.last).recover{
          case e:Exception =>
            failed(e)
            throw new StageException("",e)
        }.map{
          last =>
            ok(id)
            fetchDocs
        }
    }

  }

  def fetchDocs: Source[Document, Any] ={
    MongoSource(mongoCollection.find(json("stageInfo.id"->id)))
  }


  override val phase: String = "absorb"
}


class ConvertStage(override val id:String, mongoCollection: MongoCollection[Document], converter:Converter)(override implicit val log:Logger) extends Stage {

  def convert(doc:Document)(implicit executor: ExecutionContext): Future[Map[String,util.Map[String,Object]]] ={
    Future{
      converter.convert(doc).asScala.toMap
    }
  }

  def process(source:Source[Document, Any])(implicit executor: ExecutionContext, materializer:Materializer): Future[Source[Map[String,Map[String,Object]], Any]] = {

    status match {

      case 3 =>
        Future {
          fetchDocs
        }
      case 0 | 1 =>
        status = 2
        source.mapAsync(3) {
          doc =>
            convert(doc).flatMap {
              converted =>
                val doc = new Document(converted + ("stageInfo" -> Map("id" -> id, "updateTime" -> updateTime)))
                mongoCollection.insertOne(doc).asFuture
            }
        }.runWith(Sink.last).recover {
          case e: Exception =>
            failed(e)
            throw new StageException("", e)
        }.map {
          _ =>
            ok(id)
            fetchDocs
        }
    }
  }

  def fetchDocs: Source[Map[String, Map[String, Object]], Any] ={
    MongoSource(mongoCollection.find(json("stageInfo.id"->id))).map {
      doc =>
        doc.asScala.toMap.map {
          case (key: String, m: java.util.Map[String, Object]) =>
            (key, m.asScala.toMap)
        }
    }
  }

  override val phase: String = "convert"

}


class MergeStage(override val id:String, keyMap:util.Map[String, util.List[String]], entitySink:EntitySink)(override implicit val log:Logger) extends Stage {

  val identifier:Identifier = new Identifier(new KeyRuleInMap(keyMap))


  def merge(input: Map[String,Map[String,Object]]): Seq[Entity] = {
      input.map{
        case (name, value)=>
          identifier.genKey(name, value)
      }.toSeq

  }

  def process(source:Source[Map[String,Map[String,Object]], Any])(implicit executor: ExecutionContext, materializer:Materializer): Future[Done] = {
    status = 2
    source.map{
      doc =>
        merge(doc)
    }.mapAsync(10){
      seq =>
        Future.sequence( seq.map(entity => {
          val promise = Promise[String]()
          entitySink.asyncUpdate(entity,promise)
          promise.future
        }))
    }.runWith(Sink.foreach(println)).recover{
      case e:Exception =>
        failed(e)
        throw new StageException("",e)
    }.map{
      _ =>
        ok(id)
        Done
    }
  }


  override val phase: String = "merge"


}