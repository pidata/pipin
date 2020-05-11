package io.pipin.core.domain

import java.util

import akka.stream.Materializer
import akka.{Done, NotUsed}
import akka.stream.alpakka.mongodb.scaladsl.MongoSource
import akka.stream.scaladsl.{Keep, Sink, Source, SourceQueueWithComplete}
import com.mongodb.client.model.FindOneAndUpdateOptions
import org.slf4j.Logger
import io.pipin.core.exception.StageException

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success, Try}
import com.mongodb.reactivestreams.client.{FindPublisher, MongoCollection}
import org.bson.Document
import io.pipin.core.ext._
import io.pipin.core.Converters._
import io.pipin.core.util.Hashing

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
    log.error(s"stage $id failed", exception)
    status = 1
    result = Failure(exception)
  }

  def ok(str: String){
    log.info(s"stage $id finished")
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

  def absorb(doc:Document)(implicit executor: ExecutionContext): Future[Seq[Document]] = {

    if(doc.containsKey("_id")){
      doc.put("id", doc.get("_id"))
      doc.remove("_id")
    }

    val key = hash(doc)

    log.info("try to find and update doc with key {}", key)

    doc.asInstanceOf[util.Map[String,Object]] putAll json("key"-> key, "stageInfo" -> json("id" -> id, "updateTime" -> updateTime))

    mongoCollection.findOneAndUpdate(json("key"->key),json("$setOnInsert"->doc), new FindOneAndUpdateOptions().upsert(true)).asFuture

  }

  def hash(doc:Document):Long = {
    Math.abs(Hashing.fnvHash(doc.toJson))
  }

  def process(source:Source[Document, Any], returnLeft: Any => Unit) (implicit executor: ExecutionContext, materializer:Materializer): Future[Source[Document, Any]] ={
    log.info("======= absorb stage {} ========", id)
    status match {
      case 3 =>
        Future{
          fetchDocs
        }
      case 0|1 =>
        status = 2
        source.mapAsync(10){
          doc =>
            absorb(doc).map{
              m =>
                //log.info("absorb doc {}", doc)
                ""
            }
        }.toMat(Sink.last)(Keep.both).run() match {
          case (left, right) =>
            returnLeft(left)
            right.recover{
              case e:Exception =>
                failed(e)
                throw new StageException("stage failed",e)
            }.map{
              last =>
                ok(id)
                fetchDocs
            }
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

    log.info("======= convert stage {} ========", id)
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
              entities =>
                val doc = new Document(entities + ("stageInfo" -> json("id" -> id, "updateTime" -> updateTime)))
                //log.info("save entity {}", doc.toJson)
                mongoCollection.insertOne(doc).asFuture

            }
        }.runWith(Sink.last).recover {
          case e: NoSuchElementException =>
            log.warn("empty stream")
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
        doc.asScala.toMap.filter{
          case ("stageInfo", v) =>
            false
          case (k,v:java.util.Map[String, Object]) =>
            true
          case _ =>
            false
        }.map {
          case (key: String, m: java.util.Map[String, Object]) =>
            (key, m.asScala.toMap)
        }
    }
  }

  override val phase: String = "convert"

}


class MergeStage(override val id:String, keyMap:util.Map[String, Array[String]], entitySink:EntitySink)(override implicit val log:Logger) extends Stage {

  val identifier:Identifier = new Identifier(new KeyRuleInMap(keyMap, log))


  def merge(input: Map[String,Map[String,Object]]): Seq[Entity] = {
      input.map{
        case (name, value)=>
          identifier.genKey(name, value)
      }.toSeq

  }

  def process(source:Source[Map[String,Map[String,Object]], Any])(implicit executor: ExecutionContext, materializer:Materializer): Future[Done] = {
    log.info("======= merge stage {} ========", id)
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
    }.runWith(Sink.foreach(x=>log.info("processed {} entities ", x.size))).recover{
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