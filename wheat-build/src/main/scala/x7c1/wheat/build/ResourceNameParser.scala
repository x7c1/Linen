package x7c1.wheat.build

import sbt.complete.Parser


object ResourceNameParser {
  import sbt.complete.DefaultParsers._

  def readPrefix(name: String): Either[WheatParserError, ResourcePrefix] = {
    parse(name, parserToPrefix).left.map(WheatParserError).joinRight
  }

  def identifier = WheatParser.identifier

  def parserToPrefix: Parser[Either[WheatParserError, ResourcePrefix]] = {
    val wordsParser = identifier ~ (token('_') ~> identifier).* map {
      case (x, xs) => Words(x +: xs)
    }
    any.*.string <~ token(".xml") map { raw =>
      val p = "_".? ~ (wordsParser <~ token("__")).? ~ wordsParser map {
        case ((underscore, parent), words) =>
          val parentName = parent map (_.camelize)
          ResourcePrefix(
            raw = raw,
            ofClass = (underscore getOrElse "") + (parentName getOrElse "") + words.camelize,
            ofKey = raw + "__",
            parentClassName = for (u <- underscore; p <- parentName) yield u + p
          )
      }
      parse(raw, p).left map WheatParserError
    }
  }
  private case class Words(values: Seq[String]){
    def camelize = values.map(_.capitalize).mkString
  }
}
