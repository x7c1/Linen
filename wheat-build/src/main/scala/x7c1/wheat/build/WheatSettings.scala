package x7c1.wheat.build

import sbt.Configurations.config
import sbt.{Def, File}
import x7c1.wheat.build.layout.LayoutGenerator
import x7c1.wheat.build.values.ValuesGenerator

object WheatSettings {

  lazy val packages = Def.settingKey[WheatPackages]("project packages")
  lazy val directories = Def.settingKey[WheatDirectories]("project directories")

  lazy val generateLayout = Def.inputKey[Unit]("Generates res/layout files")
  lazy val generateValues = Def.inputKey[Unit]("Generates res/values files")

  lazy val wheat = config("wheat")

  def all = Seq(
    generateLayout in wheat := LayoutGenerator.task.evaluated,
    generateValues in wheat := ValuesGenerator.task.evaluated
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
