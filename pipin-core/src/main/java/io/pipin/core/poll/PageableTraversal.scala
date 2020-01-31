package io.pipin.core.poll

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, Uri}
import akka.http.scaladsl.{Http, HttpExt}
import akka.stream.{Materializer, OverflowStrategy}
import akka.stream.scaladsl.{Source, SourceQueueWithComplete}
import akka.util.ByteString
import akka.{Done, NotUsed}
import org.slf4j.Logger
import io.pipin.core.exception.JobException
import org.bson.Document

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by libin on 2020/1/7.
  */
trait PageableTraversal  extends Traversal{
  val pageStartFrom:Int = 0
  val docSource: Source[Document, SourceQueueWithComplete[Document]] = Source.queue[Document](Integer.MAX_VALUE, OverflowStrategy.fail)

  val startUri:Uri
  val pageParameter:String
  val initQuery: Uri.Query = startUri.query()
  implicit val actorSystem:ActorSystem
  val http: HttpExt = Http()
  implicit val log: Logger

  override def start(queue:Any)(implicit executor: ExecutionContext, materializer:Materializer):Unit = {
    queue match {
      case q:SourceQueueWithComplete[Document] =>
        request(pageStartFrom, q)
    }

  }

  private def request(page:Int, queueWithComplete: SourceQueueWithComplete[Document])(implicit executor: ExecutionContext, materializer:Materializer): Unit = {
    val nextQuery: Uri.Query = Uri.Query(initQuery.map {
      case (`pageParameter`, v) =>
        (pageParameter, String.valueOf(page))
      case (k, v) =>
        (k, v)
    }: _*)

    val nextUri = Uri("http", startUri.authority, startUri.path, Some(nextQuery.toString()))

    log.info("fetch http with url {}", nextUri)

    http.singleRequest(HttpRequest(HttpMethods.GET, nextUri)).flatMap {
      res =>
        if(res.status.isSuccess())
          res.entity.dataBytes.map(_.utf8String).runReduce(_ + _)
        else{
          log.error("http error: {}", res.status.intValue())
          throw new JobException(s"http error: ${res.status.defaultMessage()}")
        }
    }
      .map(Document.parse).map {
      doc =>
        log.info("get response with {}", doc)
        getContent(doc).foreach(queueWithComplete.offer)
        if (! endPage(doc)) {
          request(page + 1, queueWithComplete)
        }else{
          queueWithComplete.complete()
        }

    }

  }

  override def stream()(implicit executor: ExecutionContext, materializer:Materializer): Source[Document, Any] = {
    docSource
  }

  def endPage(doc:Document): Boolean

  def getContent(doc:Document): Iterator[Document]

}
