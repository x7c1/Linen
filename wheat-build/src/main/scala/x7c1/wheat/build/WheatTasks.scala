package x7c1.wheat.build

import sbt.{Def, InputKey}

object WheatTasks {

  val packages = Def.settingKey[WheatPackages]("project packages")
  val directories = Def.settingKey[WheatDirectories]("project directories")

  def settings = Seq(
    InputKey[Unit]("generate-layout") := LayoutGenerator.task.evaluated
  )
}
