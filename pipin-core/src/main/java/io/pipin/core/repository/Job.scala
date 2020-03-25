package io.pipin.core.repository

import ch.qos.logback.classic.Logger
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.result.UpdateResult
import com.mongodb.reactivestreams.client.{MongoCollection, MongoDatabase}
import io.pipin.core.domain.{Job, Project, Workspace}
import io.pipin.core.settings.{ConvertSettings, MergeSettings}
import io.pipin.core.Converters._
import org.bson.Document

import scala.concurrent.Future

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

}