package io.pipin.core.poll

import java.util

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{HttpHeader, HttpMethod, HttpMethods, Uri}
import org.slf4j.Logger
import org.bson.Document

import scala.collection.JavaConverters._
import io.pipin.core.settings.PollSettings

/**
  * Created by libin on 2020/1/7.
  */
class SimpleTraversal(val uri:String,
                      override val pageParameter:String,
                      override val pageStartFrom:Int = 0,
                      pollSettings:PollSettings
                     )(override implicit val actorSystem: ActorSystem, override implicit val log: Logger) extends PageableTraversal {


  override def startUri:Uri = Uri(uri)

  /** *
    * 判断是否为最后一页的依据
    *
    * @return
    */
  override def endPage(doc:Document): Boolean ={
    val totalPages = doc.getInteger("totalPages")
    val pageNumber = doc.getInteger("number")

    pageNumber >= totalPages
  }

  override def getContent(doc: Document): Seq[Document] = {
    if(doc.containsKey(getContentField)){
      doc.get(getContentField, classOf[java.util.List[Document]]).asScala
    }else if("__entity__".equalsIgnoreCase(getContentField)){
      Seq(doc)
    } else{
      log.warn(s"entity not found in field ${getContentField} for doc ${doc}")
      Seq.empty
    }
  }

  /** *
    * 结果【列表】所在的字段
    *
    * @return
    */
  def getContentField = pollSettings.contentField

  override def getMethod: HttpMethod = {
    if("GET".equalsIgnoreCase(pollSettings.method)){
      HttpMethods.GET
    }else{
      HttpMethods.POST
    }
  }

  override def getTokenAuthorizator: TokenAuthorizator = new TokenAuthorizator{
    override def getToken: String = null
  }


  override def getBody(extraParams: util.Map[String, String]): String = ""

  override def headers: Array[Array[String]] = Array.empty

  override def onPageNext(doc: Document, params:util.Map[String, String]): Unit = {}

  def extraParamsMap: util.Map[String, String] = {
    pollSettings.extraParams.asJava
  }

  override def extraParamsBatch: Array[util.Map[String, String]] = {
    Array(extraParamsMap)
  }

}
