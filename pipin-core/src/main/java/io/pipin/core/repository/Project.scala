package io.pipin.core.repository

import com.mongodb.reactivestreams.client.MongoCollection
import io.pipin.core.domain.Project
import io.pipin.core.util.UUID
import io.pipin.core.Converters._
import org.bson.Document

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by libin on 2020/1/10.
  */
object Project {

  val collection: MongoCollection[Document] = MongoDB.db.getCollection("pipin_project")
  def apply(name:String): Project = new Project(UUID(), name)

  def applyFromDoc(doc:Document): Project = {
    val project = new Project(doc.getString("id"),doc.getString("name"))

    project
  }

  def findById(id:String)(implicit executor: ExecutionContext): Future[Project] = {
    collection.find(json("_id"->id)).asFuture.map {
      doc =>
        applyFromDoc(doc)
    }

  }
}
