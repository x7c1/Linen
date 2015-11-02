package x7c1.wheat.build.layout

import sbt._
import x7c1.wheat.build.WheatParser.selectFrom
import x7c1.wheat.build.WheatTasks.{directories, packages}
import x7c1.wheat.build._

object LayoutGenerator {

  def task = Def inputTask {
    val names = selectLayoutFiles.parsed

    println("selected files")
    names.map(" * " + _).foreach(println)

    val loader = new LayoutResourceLoader(locations.value.layoutSrc)
    val sources = names map loader.load map (_.right map applyTemplate.value)
    val write = (source: JavaSource) => {
      JavaSourceWriter write source
      println(" * " + source.file.getPath)
    }
    println("generated files")
    sources map (_.right foreach (_ foreach write))
  }

  def selectLayoutFiles = Def settingDyn {
    val finder = locations.value.layoutSrc * "*.xml"
    selectFrom(finder)
  }

  def locations = Def setting LayoutLocations(
    packages = packages.value,
    directories = directories.value
  )

  def applyTemplate = Def setting { layout: ParsedResource =>
    val factory = new JavaLayoutSourcesFactory(locations.value)
    factory createFrom layout
  }
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
