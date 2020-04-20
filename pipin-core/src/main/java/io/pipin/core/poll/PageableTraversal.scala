package io.pipin.core.poll

import akka.actor.ActorSystem
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.{Authorization, OAuth2BearerToken, RawHeader}
import akka.http.scaladsl.{Http, HttpExt}
import akka.stream.{Materializer, OverflowStrategy}
import akka.stream.scaladsl.{Source, SourceQueueWithComplete}
import org.slf4j.Logger
import io.pipin.core.exception.JobException
import org.bson.Document

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}

import scala.collection.JavaConverters._

/**
  * Created by libin on 2020/1/7.
  */
trait PageableTraversal  extends Traversal{
  val pageStartFrom:Int = 0
  val docSource: Source[Document, SourceQueueWithComplete[Document]] = Source.queue[Document](Integer.MAX_VALUE, OverflowStrategy.fail)

  def startUri:Uri
  val pageParameter:String
  val initQuery: Seq[(String, String)] = (startUri.query().toMap + (pageParameter -> pageStartFrom.toString)).toSeq
  implicit val actorSystem:ActorSystem
  val http: HttpExt = Http()
  implicit val log: Logger

  override def start(queue:Any)(implicit executor: ExecutionContext, materializer:Materializer):Unit = {
    queue match {
      case q:SourceQueueWithComplete[Document] =>
        extraParamsBatch.foreach{
          extraParams =>
            request(pageStartFrom, extraParams, q)
        }
    }

  }




  private def request(page:Int, extraParams:java.util.Map[String,String], queueWithComplete: SourceQueueWithComplete[Document])(implicit executor: ExecutionContext, materializer:Materializer): Unit = {
    val nextQuery: Uri.Query = Uri.Query((initQuery ++ extraParams.asScala.toSeq).map {
      case (`pageParameter`, v) =>
        (pageParameter, String.valueOf(page))
      case (k, v) =>
        (k, v)
    }: _*)

    val nextUri = Uri("http", startUri.authority, startUri.path, Some(nextQuery.toString()))

    log.info("fetch http with url {}", nextUri)

    val headers = getHeaders

    http.singleRequest(HttpRequest(getMethod, nextUri).withEntity(ContentTypes.`application/json`, getEntityBody(extraParams)).withHeaders(headers)).flatMap {
      res =>
        if(res.status.isSuccess())
          res.entity.dataBytes.map(_.utf8String).runReduce(_ + _)
        else{
          log.error("http error: {} method: {}", res.status.intValue(), getMethod)
          res.entity.dataBytes.map(_.utf8String).runReduce(_ + _).foreach(log.error)
          headers.foreach(h=>log.error(h.toString()))
          throw new JobException(s"http error: ${res.status.defaultMessage()}")
        }
    }
      .map(Document.parse).map {
      doc =>
        log.info("get response with {}", doc)
        getContent(doc).foreach(queueWithComplete.offer)
        if (! endPage(doc)) {
          onPageNext(doc, extraParams)
          request(page + 1, extraParams, queueWithComplete)
        }else{
          queueWithComplete.complete()
        }

    }

  }

  override def stream()(implicit executor: ExecutionContext, materializer:Materializer): Source[Document, Any] = {
    docSource
  }

  private def getEntityBody(extraParams:java.util.Map[String,String]):String = {
    if (HttpMethods.POST.equals(getMethod)){
      getBody(extraParams)
    }else{
      ""
    }
  }

  def endPage(doc:Document): Boolean

  def getContent(doc:Document): Iterator[Document]

  def getTokenAuthorizator:TokenAuthorizator

  def getHeaders:immutable.Seq[HttpHeader] = {

    val httpHeaders = headers.map{
      ar =>
        RawHeader(ar(0), ar(1))
    }.to[immutable.Seq]
    val token = getTokenAuthorizator.getToken
    if(null != token){
      httpHeaders :+ Authorization(OAuth2BearerToken(token))
    }else{
      httpHeaders
    }

  }

  def getMethod:HttpMethod

  def headers:Array[Array[String]]

  def getBody(extraParams:java.util.Map[String,String]):String

  def onPageNext(doc:Document, params:java.util.Map[String,String]):Unit

  def extraParamsMap:java.util.Map[String,String]

  def extraParamsBatch:Array[java.util.Map[String,String]]

}
