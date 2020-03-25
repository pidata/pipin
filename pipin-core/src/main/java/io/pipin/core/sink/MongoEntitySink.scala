package io.pipin.core.sink

import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.reactivestreams.client.{MongoCollection, MongoDatabase, Success}
import io.pipin.core.Converters.json
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
    val key = entity.key
    val doc = new Document(entity.value)
    doc.put("key", key)
    collection.findOneAndUpdate(json("key"->key),
      json("$set"->doc),
      new FindOneAndUpdateOptions().upsert(true)).subscribe(new Subscriber[Document] {
      override def onError(throwable: Throwable): Unit = {}

      override def onComplete(): Unit = {
        promise.success("done")
      }

      override def onNext(t: Document): Unit = {}

      override def onSubscribe(subscription: Subscription): Unit = {
        subscription.request(Integer.MAX_VALUE)
      }
    })
  }
}
