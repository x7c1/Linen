package x7c1.wheat.build.values

import sbt._
import x7c1.wheat.build.WheatSettings.{directories, packages, wheat}
import x7c1.wheat.build.{FilesGenerator, WheatDirectories, WheatPackages}

object ValuesGenerator {
  def task = {
    val generator = new FilesGenerator(
      finder = Def setting locations.value.valuesSrc * "*.xml",
      loader = Def setting new ValuesResourceLoader(locations.value.valuesSrc),
      generator = Def setting new ValuesSourcesFactory(locations.value)
    )
    generator.task
  }
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
}
