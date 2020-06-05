package io.pipin.core.poll

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import io.pipin.core.PipinSystem
import io.pipin.core.domain.Project
import io.pipin.core.repository.Job
import io.pipin.core.settings.PollSettings
import io.pipin.core.util.UUID
import org.slf4j.Logger

import scala.concurrent.{Future, Promise}
import scala.reflect.runtime.{universe => ru}
import scala.util.Try

class PollWorker(project: Project) {

  def execute(): Future[String] = {
    implicit val log: Logger = project.workspace.getLogger("Poll")
    try {
      log.info("starting project {}", project._id)
      implicit val actorSystem = PipinSystem.actorSystem
      implicit val materialize: Materializer = ActorMaterializer()
      implicit val executionContext = actorSystem.dispatchers.lookup("poll-dispatcher")
      val traversal = project.traversal

      Job(UUID(), project, project.convertSettings, project.mergeSettings)
        .process(traversal.stream(), traversal.start)
    }catch {
      case e:Throwable =>
        log.error(s"job execution failed for project ${project._id}", e)
        throw e
    }
  }
}

