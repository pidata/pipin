package io.pipin.core.repository

import ch.qos.logback.classic.Logger
import io.pipin.core.domain.{Job, Project, Workspace}
import io.pipin.core.settings.{ConvertSettings, MergeSettings}

/**
  * Created by libin on 2020/1/9.
  */
object Job {
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