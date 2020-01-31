package io.pipin.core.poll

import akka.stream.{ActorMaterializer, Materializer}
import io.pipin.core.PipinSystem
import io.pipin.core.domain.Project
import io.pipin.core.repository.Job
import io.pipin.core.settings.PollSettings
import io.pipin.core.util.UUID
import org.slf4j.Logger


class PollWorker(project: Project) {

  def execute(): Unit = {
    implicit val actorSystem = PipinSystem.actorSystem
    implicit val materialize:Materializer =  ActorMaterializer()
    implicit val executionContext = actorSystem.dispatchers.lookup("poll-dispatcher")
    val pollSettings: PollSettings = project.pollSettings
    implicit val log: Logger = project.workspace.getLogger("Poll")
    val traversal = new SimpleTraversal(pollSettings.startUri, pollSettings.pageParameter, pollSettings.pageStartFrom)


    Job(UUID(),project, project.convertSettings, project.mergeSettings).process(traversal.stream(), traversal.start)


  }
}

