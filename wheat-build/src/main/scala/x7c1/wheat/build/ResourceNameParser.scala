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
      val p = (wordsParser <~ token("__")).? ~ wordsParser map {
        case (parent, words) =>
          val parentName = parent map (_.camelize)
          ResourcePrefix(
            raw = raw,
            ofClass = (parentName getOrElse "") + words.camelize,
            ofKey = raw + "__",
            parentClassName = parentName
          )
      }
      parse(raw, p).left map WheatParserError
    }
  }
  private case class Words(values: Seq[String]){
    def camelize = values.map(_.capitalize).mkString
  }
}
