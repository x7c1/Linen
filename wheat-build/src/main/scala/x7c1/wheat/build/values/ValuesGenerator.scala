package x7c1.wheat.build.values

import sbt._
import x7c1.wheat.build.WheatParser.selectFrom
import x7c1.wheat.build.WheatTasks.{directories, packages}
import x7c1.wheat.build.{WheatDirectories, WheatPackages}

object ValuesGenerator {
  def task = Def inputTask {

    val names = selectValuesFiles.parsed
    println("selected files")
    names.map(" * " + _).foreach(println)
  }

  def selectValuesFiles = Def settingDyn {
    val finder = locations.value.valuesSrc * "strings_*.xml"
    selectFrom(finder)
  }

  def locations = Def setting ValuesLocation(
    packages = packages.value,
    directories = directories.value
  )
}

case class ValuesLocation(
  packages: WheatPackages,
  directories: WheatDirectories){

  val valuesSrc: File = directories.starter / "src/main/res/values"

  val valuesDst = {
    directories.glue / "src/main/java" / packages.glueLayout
  }
}
