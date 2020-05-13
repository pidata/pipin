package io.pipin.core.repository

import ch.qos.logback.classic.Logger
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.result.UpdateResult
import com.mongodb.reactivestreams.client.{MongoCollection, MongoDatabase}
import io.pipin.core.domain.{AbsorbStage, ConvertStage, Job, MergeStage, Project, Workspace}
import io.pipin.core.settings.{ConvertSettings, MergeSettings}
import io.pipin.core.Converters._
import io.pipin.core.repository.Project.{applyFromDoc, collection}
import org.bson.Document

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by libin on 2020/1/9.
  */
object Job {



  val db: MongoDatabase =  MongoDB.db("pipin_meta")

  val dbCollection: MongoCollection[Document] = db.getCollection("job")

  def save(job: Job): Future[Seq[UpdateResult]] = dbCollection.updateOne(json("_id"->job.id), json("$set"->job.toDocument), new UpdateOptions().upsert(true)).asFuture

  def apply(id: String, project: Project,
            convertSettings: ConvertSettings, mergeSettings: MergeSettings): Job = {
    val workspace = project.workspace
    new Job(id, project,workspace,
      Stage.applyAbsorbStage(workspace.getLogger(s"absorb")),
      Stage.applyConvertStage(convertSettings)(workspace.getLogger(s"convert")),
      Stage.applyMergeStage(mergeSettings)(workspace.getLogger(s"merge")))
  }

  def apply(id: String, project: Project): Job = {
    apply(id, project, project.convertSettings, project.mergeSettings)
  }

  var pageSize: Int = 10

  def findAll(page:Int)(implicit executor: ExecutionContext): Future[Seq[Document]] = {
    val offset = page * pageSize
    dbCollection.find().skip(offset).limit(pageSize).asFuture
  }

  def findByProject(projectId:String, page:Int)(implicit executor: ExecutionContext): Future[Seq[Document]] = {
    val offset = page * pageSize
    dbCollection.find(json("project._id"->projectId)).skip(offset).limit(pageSize).asFuture
  }


  def findById(id:String)(implicit executor: ExecutionContext): Future[Option[Document]] = {
    dbCollection.find(json("_id"->id)).asFuture.map(_.headOption)
  }


  private def applyFromDoc(doc:Document, project: Project): Job = {
    val id = doc.getString("_id")
    val workspace = project.workspace
    val absorbStage = Stage.applyFromDoc(doc.get("absorbStage").asInstanceOf[Document], project)(workspace.getLogger).asInstanceOf[AbsorbStage]
    val convertStage = Stage.applyFromDoc(doc.get("convertStage").asInstanceOf[Document], project)(workspace.getLogger).asInstanceOf[ConvertStage]
    val mergeStage = Stage.applyFromDoc(doc.get("mergeStage").asInstanceOf[Document], project)(workspace.getLogger).asInstanceOf[MergeStage]
    val job = new Job(id, project, project.workspace, absorbStage, convertStage, mergeStage)
    job.startTime = doc.getLong("startTime")
    job.status = doc.getInteger("status")
    job
  }

  def applyFromDoc(doc:Document)(implicit executionContext: ExecutionContext): Future[Option[Job]] = {

    val projectId = doc.get("project").asInstanceOf[Document].getString("_id")
    Project.findById(projectId).map{
      case Some(project)=>
        Some(applyFromDoc(doc, project))
      case None =>
        None
    }
  }

}