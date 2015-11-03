package x7c1.wheat.build

import sbt.complete.DefaultParsers._

object ResourceLoader {
  def apply(elementsLoader: ResourceElementsLoader): ResourceLoader = {
    new ResourceLoaderImpl(elementsLoader)
  }
 }

trait ResourceLoader {
  def load(fileName: String): Either[Seq[WheatParserError], ParsedResource]
}

private class ResourceLoaderImpl(elementsLoader: ResourceElementsLoader)
  extends ResourceLoader {

  override def load(fileName: String): Either[Seq[WheatParserError], ParsedResource] = {
    ResourceNameParser.readPrefix(fileName) match {
      case Right(prefix) =>
        val (l, r) = elementsLoader.create(prefix.ofKey).partition(_.isLeft)
        val errors = l.map(_.left.get)
        if (errors.isEmpty) {
          Right apply ParsedResource(prefix = prefix, elements = r.map(_.right.get))
        } else {
          Left apply errors
        }
      case Left(error) => Left(Seq(error))
    }
  }
}

trait ResourceElementsLoader {
  def create(prefix: String):
    List[Either[WheatParserError, ParsedResourceElement]]

  def camelCase(string: String): Either[WheatParserError, String] = {
    val alphabet = token('a' to 'z') | token('A' to 'Z')
    val parser = (alphabet.+.string ~ (token('_') ~> alphabet.+.string).*) map {
      case (head, tail) => head + tail.map(_.capitalize).mkString
    }
    parse(string, parser).left.map(WheatParserError)
  }
}
