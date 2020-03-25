package io.pipin.core.repository

import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.reactivestreams.client.MongoCollection
import io.pipin.core.domain.{JobTrigger, Project}
import io.pipin.core.util.UUID
import io.pipin.core.Converters._
import io.pipin.core.settings.{ConvertSettings, MergeSettings, PollSettings}
import org.bson.Document

import scala.concurrent.{ExecutionContext, Future}
import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.native.Serialization.{read, write}
import io.pipin.core.Converters._

/**
  * Created by libin on 2020/1/10.
  */
object Project {

  private val collection: MongoCollection[Document] = MongoDB.db("pipin_meta").getCollection("pipin_project")

  private implicit val formats = new DefaultFormats {
  }


  private def applyFromDoc(doc:Document): Project = {
    implicit val formats = DefaultFormats
    val pollSettings = parse(doc.get("pollSettings",classOf[Document]).toJson()).extract[PollSettings]
    val convertSettings = parse(doc.get("convertSettings",classOf[Document]).toJson()).extract[ConvertSettings]
    val mergeSettings = parse(doc.get("mergeSettings",classOf[Document]).toJson()).extract[MergeSettings]

    val project = new Project(doc.getString("_id"),doc.getString("name"))
    if(doc.containsKey("jobTrigger")){
      val jobTrigger = parse(doc.get("jobTrigger", classOf[Document]).toJson).extract[JobTrigger]
      project.jobTrigger = jobTrigger
    }
    project.pollSettings = pollSettings
    project.convertSettings = convertSettings
    project.mergeSettings = mergeSettings
    project
  }

  def save(project:Project): Future[Seq[Document]] ={

    val doc = toDocument(project)
    collection.findOneAndUpdate(json("_id"->project._id),json("$set"->doc), new FindOneAndUpdateOptions().upsert(true)).asFuture

  }

  def toDocument(project:Project): Document ={
    implicit val formats = DefaultFormats
    val doc = new Document(
      Map(
        "_id"->project._id,
        "name"->project.name,
        "source"->project.source
      ))

    val a = Document.parse(write(project.pollSettings))
    doc.put("pollSettings", Document.parse(write(project.pollSettings)))
    doc.put("convertSettings", Document.parse(write(project.convertSettings)))
    doc.put("mergeSettings", Document.parse(write(project.mergeSettings)))

    if(null != project.jobTrigger){
      doc.put("jobTrigger", Document.parse(write(project.jobTrigger)))
    }
    doc
  }

  def findById(id:String)(implicit executor: ExecutionContext): Future[Project] = {
    collection.find(json("_id"->id)).asFuture.map {
      case Seq(doc) =>
        applyFromDoc(doc)
    }

  }

  def findAllWithCron()(implicit executor: ExecutionContext): Future[Seq[Project]] = {
    collection.find(json("jobTrigger"->json("$ne"->null))).asFuture.map {
      seq =>
        seq.map(applyFromDoc)
    }

  }
}
