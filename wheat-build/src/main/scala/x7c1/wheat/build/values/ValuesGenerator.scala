package x7c1.wheat.build.values

import sbt._
import x7c1.wheat.build.WheatSettings.{directories, packages, wheat}
import x7c1.wheat.build.{FilesGenerator, WheatDirectories, WheatPackages}

object ValuesGenerator {

  def task: Def.Initialize[InputTask[Unit]] = Def settingDyn generator.value.task

  def generator = Def setting new FilesGenerator(
    finder = locations.value.valuesSrc * "*.xml",
    loader = new ValuesResourceLoader(locations.value.valuesSrc),
    generator = new ValuesSourcesFactory(locations.value)
  )
  def locations = Def setting ValuesLocations(
    packages = (packages in wheat).value,
    directories = (directories in wheat).value
  )
}

case class ValuesLocations(
  packages: WheatPackages,
  directories: WheatDirectories){

  val valuesSrc: File = directories.starter / "src/main/res/values"

  val valuesDst: File = {
    directories.glue / "src/main/java" / packages.glueValues.replace(".", "/")
  }
  val providerDst: File = {
    directories.starter / "src/main/java" / packages.starterValues.replace(".", "/")
  }

  override def toString = {
    val lines = Seq(
      "(",
      "  source",
      "    values => " + valuesSrc,
      "  destinations",
      "    interface => " + valuesDst,
      "    provider => " + providerDst,
      ")"
    )
    lines mkString "\n"
  }
}
