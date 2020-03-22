package io.pipin.core.sink

import com.mongodb.reactivestreams.client.{MongoCollection, MongoDatabase, Success}
import io.pipin.core.ext.{Entity, EntitySink}
import io.pipin.core.repository.MongoDB
import org.bson.Document
import org.reactivestreams.{Subscriber, Subscription}

import scala.concurrent.{ExecutionContext, Promise}

/**
  * Created by libin on 2020/3/22.
  */

/*
*
*/
class MongoEntitySink extends EntitySink{
  private val db: MongoDatabase = MongoDB.db
  override def asyncUpdate(entity: Entity, promise: Promise[String])(implicit executor: ExecutionContext): Unit = {
    val collection = db.getCollection(entity.name)
    collection.insertOne(new Document(entity.value)).subscribe(new Subscriber[Success]() {
      override def onSubscribe(subscription: Subscription): Unit = {
        subscription.request(Integer.MAX_VALUE)
      }

      override def onNext(success: Success): Unit = {
        promise.success("done")
      }

      override def onError(throwable: Throwable): Unit = {
      }

      override def onComplete(): Unit = {
      }
    })
  }
}
