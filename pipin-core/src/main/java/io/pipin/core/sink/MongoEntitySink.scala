package io.pipin.core.sink

import com.mongodb.client.model.{FindOneAndUpdateOptions, UpdateOptions}
import com.mongodb.client.result.UpdateResult
import com.mongodb.reactivestreams.client.{MongoCollection, MongoDatabase, Success}
import io.pipin.core.Converters.json
import io.pipin.core.ext.{Entity, EntitySink}
import io.pipin.core.repository.MongoDB
import org.bson.Document
import org.reactivestreams.{Subscriber, Subscription}
import org.slf4j.Logger

import scala.concurrent.{ExecutionContext, Promise}

/**
  * Created by libin on 2020/3/22.
  */

/*
*
*/
class MongoEntitySink(log:Logger) extends EntitySink(log:Logger){


  def database:String = MongoDB.defaultDatabase

  protected val db: MongoDatabase = MongoDB.db(database)

  def collectionName(entity: Entity): String = entity.name

  def prepareDoc(doc: Document): Unit ={
    doc.remove("_id")
  }

  def uniqueField = "key"

  override def asyncUpdate(entity: Entity, promise: Promise[String])(implicit executor: ExecutionContext): Unit = {
    val collection = db.getCollection(collectionName(entity))
    val key = entity.key
    val doc = new Document(entity.value)
    doc.put("key", key)
    prepareDoc(doc)
    collection.updateMany(json(uniqueField->doc.getString(uniqueField)),
      json("$set"->doc),
      new UpdateOptions().upsert(true)).subscribe(new Subscriber[UpdateResult] {
      override def onError(throwable: Throwable): Unit = {
        promise.failure(throwable)
        log.error("mongo update failed, " + key, throwable)
      }

      override def onComplete(): Unit = {
        if( ! promise.isCompleted){
          promise.success("done")
        }
        log.debug("updated entity " + doc.toJson)
      }

      override def onNext(t: UpdateResult): Unit = {
        if( ! promise.isCompleted){
          promise.success("done")
        }
        log.info("updated {} entity: {} = {}" ,"" + t.getModifiedCount, uniqueField, doc.getString(uniqueField))
      }

      override def onSubscribe(subscription: Subscription): Unit = {
        subscription.request(Integer.MAX_VALUE)
      }
    })
  }
}
