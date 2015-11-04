package x7c1.wheat.build

import sbt.Configurations.config
import sbt.{Def, File}
import x7c1.wheat.build.layout.{LayoutLocations, LayoutGenerator}
import x7c1.wheat.build.values.{ValuesLocations, ValuesGenerator}

object WheatSettings {

  lazy val packages = Def.settingKey[WheatPackages]("project packages")
  lazy val directories = Def.settingKey[WheatDirectories]("project directories")

  lazy val generateLayout = Def.inputKey[Unit]("Generates res/layout files")
  lazy val generateValues = Def.inputKey[Unit]("Generates res/values files")

  lazy val valuesLocations = Def.settingKey[ValuesLocations]("res/values locations")
  lazy val layoutLocations = Def.settingKey[LayoutLocations]("res/values locations")

  lazy val wheat = config("wheat")

  def all = Seq(
    generateLayout in wheat := LayoutGenerator.task.evaluated,
    generateValues in wheat := ValuesGenerator.task.evaluated,

    valuesLocations in wheat := ValuesGenerator.locations.value,
    layoutLocations in wheat := LayoutGenerator.locations.value
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
