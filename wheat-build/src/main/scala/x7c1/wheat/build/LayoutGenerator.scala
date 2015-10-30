package x7c1.wheat.build

import sbt._
import x7c1.wheat.build.WheatParser.selectFrom
import x7c1.wheat.build.WheatTasks.{directories, packages}

object LayoutGenerator {

  def task = Def inputTask {
    val names = selectLayoutFiles.parsed

    println("selected files")
    names.map(" * " + _).foreach(println)

    val loader = new ResourceLoader(locations.value.layoutSrc)
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
    val sources = new JavaLayoutSourcesFactory(locations.value)
    sources createFrom layout
  }
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

case class LayoutLocations(
  packages: WheatPackages,
  directories: WheatDirectories){

  val layoutSrc: File = {
    directories.starter / "src/main/res/layout"
  }
  val layoutDst: File = {
    directories.glue / "src/main/java" / packages.glueLayout.replace(".", "/")
  }
  val providerDst: File = {
    directories.starter / "src/main/java" / packages.starterLayout.replace(".", "/")
  }
}

case class ResourcePrefix(
  raw: String,
  ofClass: String,
  ofKey: String
)

case class ParsedResource(
  prefix: ResourcePrefix,
  elements: Seq[ParsedResourceElement]
)

case class ParsedResourceElement(
  key: String,
  label: String,
  tag: String
)
