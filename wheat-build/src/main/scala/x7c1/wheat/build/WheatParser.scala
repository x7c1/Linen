package x7c1.wheat.build

import sbt.complete.Parser
import sbt.{Def, PathFinder, State}

object WheatParser {

  import sbt.complete.DefaultParsers._

  lazy val identifier: Parser[String] = {
    val alphabet = token('a' to 'z')
    val numbers = token('0' to '9')
    alphabet.+.string ~ (numbers | alphabet).*.string map {
      case (a, b) => a + b
    }
  }

  def camelizeTail(string: String): Either[WheatParserError, String] = {
    val parser = (identifier ~ (token('_') ~> identifier).*) map {
      case (head, tail) => head + tail.map(_.capitalize).mkString
    }
    parse(string, parser).left.map(WheatParserError)
  }

  def selectFrom(finder: PathFinder): Def.Initialize[State => Parser[Seq[String]]] =
    Def.setting { state =>
      val names = finder.get.map(_.getName) filterNot (_ startsWith "_")
      ReductiveParser from names
    }

}

case class WheatParserError(message: String)
