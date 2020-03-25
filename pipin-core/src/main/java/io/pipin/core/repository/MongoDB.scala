package io.pipin.core.repository

import com.mongodb.ConnectionString
import com.mongodb.reactivestreams.client.{MongoClient, MongoClients, MongoDatabase}
import com.typesafe.config.{Config, ConfigFactory}

/**
  * Created by libin on 2020/1/6.
  */
object MongoDB {
  val config:Config = ConfigFactory.load()
  val (url) = config.getString("mongo.url")
  val mongoClient:MongoClient =  MongoClients.create(new ConnectionString(url))
  def db: MongoDatabase ={
    db(config.getString("mongo.database"))
  }

  def db(db:String): MongoDatabase ={
    mongoClient.getDatabase(db)
  }
}
