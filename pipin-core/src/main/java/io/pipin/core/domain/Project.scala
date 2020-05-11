package io.pipin.core.domain

import java.util

import akka.actor.ActorSystem
import io.pipin.core.poll.Traversal
import io.pipin.core.settings.{ConvertSettings, MergeSettings, PollSettings}
import org.slf4j.Logger

import scala.reflect.runtime.{universe => ru}

/**
  * Created by libin on 2020/1/3.
  */
class Project(val _id:String, var name:String, var source:String = "poll", keys:Array[String] = Array() ) {
  private val entity = name.replace(" ","_")
  var convertSettings:ConvertSettings = ConvertSettings(entity)
  var mergeSettings:MergeSettings = MergeSettings(Map(entity -> keys), "io.pipin.core.sink.MongoEntitySink")
  var pollSettings: PollSettings = PollSettings("", "", 0, "")
  val workspace = Workspace(id = _id)
  var jobTrigger:JobTrigger = _

  def traversal(implicit actorSystem:ActorSystem, log:Logger = workspace.getLogger("Poll")): Traversal = {
    val classMirror = ru.runtimeMirror(getClass.getClassLoader)
    val classTest = classMirror.staticClass(pollSettings.traversalClass)
    val cls1 = classMirror.reflectClass(classTest)
    val constructor = cls1.reflectConstructor(classTest.primaryConstructor.asMethod)
    constructor.apply(pollSettings.startUri, pollSettings.pageParameter, pollSettings.pageStartFrom, pollSettings, actorSystem, log).asInstanceOf[Traversal]
  }
}
