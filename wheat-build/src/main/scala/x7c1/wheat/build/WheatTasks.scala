package x7c1.wheat.build

import sbt.{File, Def}
import x7c1.wheat.build.layout.LayoutGenerator
import x7c1.wheat.build.values.ValuesGenerator

object WheatTasks {

  val packages = Def.settingKey[WheatPackages]("project packages")
  val directories = Def.settingKey[WheatDirectories]("project directories")

  val generateLayout = Def.inputKey[Unit]("Generates res/layout files")
  val generateValues = Def.inputKey[Unit]("Generates res/values files")

  def settings = Seq(
    generateLayout := LayoutGenerator.task.evaluated,
    generateValues := ValuesGenerator.task.evaluated
  )
}

case class WheatDirectories(
  starter: File,
  glue: File
)

case class WheatPackages(
  starter :String,
  starterLayout: String,
  starterValues: String,
  glueLayout: String,
  glueValues: String
)
