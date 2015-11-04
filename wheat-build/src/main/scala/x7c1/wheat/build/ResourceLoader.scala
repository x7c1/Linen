package x7c1.wheat.build

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
}
