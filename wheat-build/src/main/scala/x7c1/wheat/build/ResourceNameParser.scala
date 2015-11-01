package x7c1.wheat.build


object ResourceNameParser {
  import sbt.complete.DefaultParsers._

  def readPrefix(name: String): Either[WheatParserError, ResourcePrefix] = {
    parse(name, parserToPrefix).left.map(WheatParserError).joinRight
  }

  private def parserToPrefix =
    any.+.string <~ token(".xml") map {
      prefix => WheatParser.toCamelCase(prefix).right map {
        camel => ResourcePrefix(prefix, camel, prefix + "__")
      }
    }
}
