package x7c1.wheat.build.values

import sbt._
import x7c1.wheat.build.WheatParser.selectFrom
import x7c1.wheat.build.WheatSettings.{directories, packages}
import x7c1.wheat.build.{JavaSource, JavaSourceWriter, ParsedResource, WheatDirectories, WheatPackages}

object ValuesGenerator {
  def task = Def inputTask {

    val names = selectValuesFiles.parsed
    println("selected files")
    names.map(" * " + _).foreach(println)

    val loader = new ValuesResourceLoader(locations.value.valuesSrc)
    val sources = names map loader.load map (_.right map applyTemplate.value)
    val write = (source: JavaSource) => {
      JavaSourceWriter write source
      println(" * " + source.file.getPath)
    }
    println("generated files")
    sources map (_.right foreach(_ foreach write))
  }

  def selectValuesFiles = Def settingDyn {
    val finder = locations.value.valuesSrc * "*.xml"
    selectFrom(finder)
  }

  def locations = Def setting ValuesLocations(
    packages = packages.value,
    directories = directories.value
  )

  def applyTemplate = Def setting { values: ParsedResource =>
    val factory = new ValuesSourcesFactory(locations.value)
    factory createFrom values
  }
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
}
