package x7c1.wheat.build

import sbt.Def.{Initialize, inputTask}
import sbt.{Def, PathFinder}
import x7c1.wheat.build.WheatParser.selectFrom

class FilesGenerator (
  finder: Initialize[PathFinder],
  loader: Initialize[ResourceLoader],
  generator: Initialize[JavaSourcesFactory] ){

  def selector = Def settingDyn selectFrom(finder.value)

  def task = inputTask {
    val names = selector.parsed

    println("selected files")
    names.map(" * " + _).foreach(println)

    val list = names map loader.value.load map (_.right map generator.value.createFrom)
    println("generated files")

    list foreach {
      case Right(sources) =>
        sources foreach { source =>
          JavaSourceWriter write source
          println(" * " + source.file.getPath)
        }
      case Left(errors) => errors.foreach(println)
    }
  }
}
