package io.pipin.core.repository

import java.util

import org.slf4j.Logger
import com.mongodb.reactivestreams.client.MongoClient
import io.pipin.core.domain._
import io.pipin.core.settings.{ConvertSettings, MergeSettings}
import io.pipin.core.util.UUID
import io.pipin.core.Converters._
import org.bson.Document

import scala.collection.JavaConverters._
import scala.concurrent.Future

/**
  * Created by libin on 2020/1/3.
  */
object Stage {
  val db =  MongoDB.db("pipin")
  val pageSize = 10

  def applyAbsorbStage(implicit log:Logger):AbsorbStage = {
     new AbsorbStage(UUID(), db.getCollection("absorb"))(log)
  }

  def applyConvertStage(convertSettings: ConvertSettings)(implicit log:Logger):ConvertStage = {
    new ConvertStage(UUID(), db.getCollection("convert"), convertSettings.converter)
  }

  def applyMergeStage(mergeSettings: MergeSettings)(implicit log:Logger):MergeStage =
    new MergeStage(UUID(), mergeSettings.keyMap.asJava, mergeSettings.entitySink(log))


  def applyFromDoc(doc:Document, project: Project)(implicit getLogger:(String)=>Logger): Stage ={
    val (id) = doc.getString("id")
    val (convertSettings, mergeSettings) = (project.convertSettings, project.mergeSettings)
    doc.getString("phase") match {
      case "absorb" =>
        new AbsorbStage(id, db.getCollection("absorb"))(getLogger("absorb"))
      case "convert" =>
        new ConvertStage(id, db.getCollection("convert"), convertSettings.converter)(getLogger("convert"))
      case "merge" =>
        new MergeStage(id, mergeSettings.keyMap.asJava, mergeSettings.entitySink(getLogger("merge")))(getLogger("merge"))
    }

  }

  def findStageById(stageId:String, page:Int = 0): Future[Seq[Document]] = {
    val offset = page * pageSize
    db.getCollection("absorb").find(json("stageInfo.id"->stageId)).skip(offset).limit(pageSize).asFuture
  }

}
