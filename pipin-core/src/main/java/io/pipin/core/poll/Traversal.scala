package io.pipin.core.poll

import akka.NotUsed
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import akka.util.ByteString
import org.bson.Document

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by libin on 2020/1/10.
  */
trait Traversal {
  def stream()(implicit executor: ExecutionContext, materializer:Materializer): Source[Document,Any]
  def start(queue:Any)(implicit executor: ExecutionContext, materializer:Materializer):Unit
}
