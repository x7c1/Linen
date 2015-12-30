package x7c1.wheat.build

import sbt.{State, Def, PathFinder}
import sbt.complete.Parser

object WheatParser {
  import sbt.complete.DefaultParsers._

  def exclusiveParser(items: Seq[String]): Parser[Seq[String]] = {
    val base = items match {
      case Nil => failure("item not remain")
      case _ => items.map(token(_)).reduce(_ | _)
    }
    val recurse = (Space ~> base) flatMap { item =>
      val (consumed, remains) = items.partition(_ == item)
      exclusiveParser(remains) map { input => consumed ++ input }
    }
    recurse ?? Nil
  }

  lazy val identifier: Parser[String] = {
    val alphabet = token('a' to 'z')
    val numbers = token('0' to '9')
    alphabet.+.string ~ (numbers | alphabet).*.string map {
      case (a, b) => a + b
    }
  }

  def camelize(string: String): Either[WheatParserError, String] = {
    val f = (a: String, b: Seq[String]) => {
      a.capitalize + b.map(_.capitalize).mkString
    }
    val p1 = (identifier ~ (token('_') ~> identifier).*) map f.tupled
    val parser = (p1 ~ (token("__") ~> p1).*) map f.tupled

    parse(string, parser).left.map(WheatParserError)
  }

  def camelizeTail(string: String): Either[WheatParserError, String] = {
    val parser = (identifier ~ (token('_') ~> identifier).*) map {
      case (head, tail) => head + tail.map(_.capitalize).mkString
    }
    parse(string, parser).left.map(WheatParserError)
  }

  def selectFrom(finder: PathFinder): Def.Initialize[State => Parser[Seq[String]]] =
    Def.setting { state =>
      val names = finder.get.map(_.getName)
      exclusiveParser(names)
    }

}

case class WheatParserError(message: String)
