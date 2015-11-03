package x7c1.wheat.build.layout

import sbt._
import x7c1.wheat.build.WheatSettings.{directories, packages}
import x7c1.wheat.build.{FilesGenerator, WheatDirectories, WheatPackages}

object LayoutGenerator {
  def task = {
    val generator = new FilesGenerator(
      finder = Def setting locations.value.layoutSrc * "*.xml",
      loader = Def setting new LayoutResourceLoader(locations.value.layoutSrc),
      generator = Def setting new LayoutSourcesFactory(locations.value)
    )
    generator.task
  }
  def locations = Def setting LayoutLocations(
    packages = packages.value,
    directories = directories.value
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
}
