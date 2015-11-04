package x7c1.wheat.build

import sbt.Def.inputTask
import sbt.Keys.streams
import sbt.PathFinder
import x7c1.wheat.build.WheatParser.selectFrom
import x7c1.wheat.build.WheatSettings.wheat

class FilesGenerator (
  finder: PathFinder,
  loader: ResourceLoader,
  generator: JavaSourcesFactory ){

  def task = inputTask {
    val logger = WheatLogger((streams in wheat).value.log)
    val names = selectFrom(finder).parsed

    logger info "selected files"
    names.map(" * " + _).foreach(logger.info)

    val list = names map loader.load map (_.right map generator.createFrom)
    logger info "generated files"

    list foreach {
      case Right(sources) =>
        sources foreach { source =>
          JavaSourceWriter write source
          logger info " * " + source.file.getPath
        }
      case Left(errors) => errors.foreach(println)
    }
  }
}
