package io.pipin.core.poll

import akka.actor.ActorSystem
import akka.http.scaladsl.model.Uri
import org.slf4j.Logger
import org.bson.Document

import scala.collection.JavaConverters._

/**
  * Created by libin on 2020/1/7.
  */
class SimpleTraversal(override val startUri:Uri, override val pageParameter:String, override val pageStartFrom:Int = 0)(override implicit val actorSystem: ActorSystem, override implicit val log: Logger) extends PageableTraversal {


  override def endPage(doc:Document): Boolean ={
    val totalPages = doc.getInteger("totalPages")
    val pageNumber = doc.getInteger("number")

    pageNumber >= totalPages
  }

  override def getContent(doc: Document): Iterator[Document] = {
    doc.getList("content", classOf[Document]).asScala.toIterator
  }
}
