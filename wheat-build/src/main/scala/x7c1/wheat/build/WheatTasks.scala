package x7c1.wheat.build

import sbt.{Def, File}
import x7c1.wheat.build.layout.LayoutGenerator

object WheatTasks {

  val packages = Def.settingKey[WheatPackages]("project packages")
  val directories = Def.settingKey[WheatDirectories]("project directories")

  val generateLayout = Def.inputKey[Unit]("Generates res/layout files")

  def settings = Seq(
    generateLayout := LayoutGenerator.task.evaluated
  )
}

case class WheatDirectories(
  starter: File,
  glue: File
)

case class WheatPackages(
  starter :String,
  starterLayout: String,
  glueLayout: String
)
