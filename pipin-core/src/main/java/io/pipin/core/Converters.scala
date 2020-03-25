package io.pipin.core

/**
  * Created by libin on 2020/1/4.
  */

import org.bson.Document
import org.reactivestreams.{Publisher, Subscriber, Subscription}

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.concurrent.{Future, Promise}


class AsFuture[T](publisher:Publisher[T],op: Publisher[T]=> Future[Seq[T]]) {
  def asFuture: Future[Seq[T]] = op(publisher)
}

object Converters {

  def json(elems: (String, Any)*): Document ={
    new Document(convertMapToJava(Map(elems:_*)))
  }

  implicit def convertStringMapToJava(map: Map[String, String]): java.util.Map[String, Object] ={
    map.asInstanceOf[Map[String, AnyRef]].asJava
  }

  implicit def convertMapToJava(map: Map[String, Any]): java.util.Map[String, Object] ={
    map.asInstanceOf[Map[String, AnyRef]].asJava
  }

  def convertPublisherToFuture[T](publisher: Publisher[T]): Future[Seq[T]] ={
    val promise:Promise[Seq[T]] = Promise()
    val results = mutable.Set[T]()
    publisher.subscribe(new Subscriber[T]{
      override def onError(throwable: Throwable): Unit = promise.failure(throwable)

      override def onComplete(): Unit = {
        promise.success(results.toSeq)
      }

      override def onNext(t: T): Unit = {
        results.add(t)
      }

      override def onSubscribe(subscription: Subscription): Unit = subscription.request(Integer.MAX_VALUE)
    })
    promise.future
  }


  implicit def publisherConverter[T](publisher: Publisher[T]):AsFuture[T] ={
    new AsFuture(publisher, convertPublisherToFuture[T])
  }

}
