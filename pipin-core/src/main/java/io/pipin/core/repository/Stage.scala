package io.pipin.core.repository

import java.util

import org.slf4j.Logger
import com.mongodb.reactivestreams.client.MongoClient
import io.pipin.core.domain.{AbsorbStage, ConvertStage, Job, MergeStage, Stage}
import io.pipin.core.settings.{ConvertSettings, MergeSettings}
import io.pipin.core.util.UUID

import scala.collection.JavaConverters._

/**
  * Created by libin on 2020/1/3.
  */
object Stage {
  val db =  MongoDB.db

  def applyAbsorbStage(implicit log:Logger):AbsorbStage = {
    new AbsorbStage(UUID(), db.getCollection("absort"))
  }

  def applyConvertStage(convertSettings: ConvertSettings)(implicit log:Logger):ConvertStage = {
    new ConvertStage(UUID(), db.getCollection("convert"), convertSettings.converter)
  }

  def applyMergeStage(mergeSettings: MergeSettings)(implicit log:Logger):MergeStage =
    new MergeStage(UUID(), mergeSettings.keyMap, mergeSettings.entitySink)


}
