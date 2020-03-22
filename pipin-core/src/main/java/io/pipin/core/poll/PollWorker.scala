package io.pipin.core.poll

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import io.pipin.core.PipinSystem
import io.pipin.core.domain.Project
import io.pipin.core.repository.Job
import io.pipin.core.settings.PollSettings
import io.pipin.core.util.UUID
import org.slf4j.Logger

import scala.reflect.runtime.{universe => ru}

class PollWorker(project: Project) {

  def execute(): Unit = {
    implicit val actorSystem = PipinSystem.actorSystem
    implicit val materialize:Materializer =  ActorMaterializer()
    implicit val executionContext = actorSystem.dispatchers.lookup("poll-dispatcher")
    val pollSettings: PollSettings = project.pollSettings
    implicit val log: Logger = project.workspace.getLogger("Poll")
    val classMirror = ru.runtimeMirror(getClass.getClassLoader)
    val classTest = classMirror.staticClass(pollSettings.traversalClass)
    val cls1 =  classMirror.reflectClass(classTest)
    val constructor = cls1.reflectConstructor(classTest.primaryConstructor.asMethod)
    val traversal = constructor.apply(pollSettings.startUri, pollSettings.pageParameter, pollSettings.pageStartFrom, pollSettings.method, actorSystem, log).asInstanceOf[SimpleTraversal]


    Job(UUID(),project, project.convertSettings, project.mergeSettings).process(traversal.stream(), traversal.start)


  }
}

