package x7c1.wheat.build.layout

import sbt._
import x7c1.wheat.build.WheatSettings.{directories, packages, wheat}
import x7c1.wheat.build.{FilesGenerator, WheatDirectories, WheatPackages}

object LayoutGenerator {

  def task: Def.Initialize[InputTask[Unit]] = Def settingDyn generator.value.task

  def generator = Def setting new FilesGenerator(
    finder = locations.value.layoutSrc * "*.xml",
    loader = new LayoutResourceLoader(locations.value.layoutSrc),
    generator = new LayoutSourcesFactory(locations.value)
  )
  def locations = Def setting LayoutLocations(
    packages = (packages in wheat).value,
    directories = (directories in wheat).value
  )
}

case class LayoutLocations(
  packages: WheatPackages,
  directories: WheatDirectories){

  val layoutSrc: File = directories.starter / "src/main/res/layout"

  val layoutDst: File = {
    directories.glue / "src/main/java" / packages.glueLayout.replace(".", "/")
  }
  val providerDst: File = {
    directories.starter / "src/main/java" / packages.starterLayout.replace(".", "/")
  }

  override def toString = {
    val lines = Seq(
      "(",
      "  source",
      "    layout => " + layoutSrc,
      "  destinations",
      "    holder => " + layoutDst,
      "    provider => " + providerDst,
      ")"
    )
    lines mkString "\n"
  }

}
