package x7c1.wheat.build

import x7c1.wheat.build.WheatParser.camelize


object ResourceNameParser {
  import sbt.complete.DefaultParsers._

  def readPrefix(name: String): Either[WheatParserError, ResourcePrefix] = {
    parse(name, parserToPrefix).left.map(WheatParserError).joinRight
  }

  private def parserToPrefix =
    any.+.string <~ token(".xml") map {
      prefix => camelize(prefix).right map {
        camel => ResourcePrefix(prefix, camel, prefix + "__")
      }
    }
}
