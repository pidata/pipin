package io.pipin.core

/**
  * Created by libin on 2020/1/4.
  */

import org.bson.Document
import org.reactivestreams.{Publisher, Subscriber, Subscription}

import scala.collection.JavaConverters._
import scala.concurrent.{Future, Promise}


class AsFuture[T](publisher:Publisher[T],op: Publisher[T]=> Future[T] ,op2: (Publisher[T], T)=> Future[T]) {
  def asFuture: Future[T] = op(publisher)
  def asFutureWithoutResult(default:T): Future[T] = op2(publisher, default)
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

  def convertPublisherToFuture[T](publisher: Publisher[T]): Future[T] ={
    val promise:Promise[T] = Promise()
    publisher.subscribe(new Subscriber[T]{
      override def onError(throwable: Throwable): Unit = promise.failure(throwable)

      override def onComplete(): Unit = {}

      override def onNext(t: T): Unit = {
        promise.success(t)
      }

      override def onSubscribe(subscription: Subscription): Unit = subscription.request(Integer.MAX_VALUE)
    })
    promise.future
  }

  def convertPublisherWithoutResultToFuture[T](publisher: Publisher[T], obj:T): Future[T] ={
    val promise:Promise[T] = Promise()
    publisher.subscribe(new Subscriber[T]{
      override def onError(throwable: Throwable): Unit = promise.failure(throwable)

      override def onComplete(): Unit = {
        promise.success(obj)
      }

      override def onNext(t: T): Unit = {

      }

      override def onSubscribe(subscription: Subscription): Unit = subscription.request(Integer.MAX_VALUE)
    })
    promise.future
  }

  implicit def publisherConverter[T](publisher: Publisher[T]):AsFuture[T] ={
    new AsFuture(publisher, convertPublisherToFuture[T], convertPublisherWithoutResultToFuture[T])
  }

}
