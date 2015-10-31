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

  def toCamelCase(x: String): Either[WheatParserError, String] = {
    val alphabet = token('a' to 'z') | token('A' to 'Z')
    val parser = (alphabet.+.string <~ token('_').?).+ map {
      _.map(_.capitalize).mkString
    }
    parse(x, parser).left.map(WheatParserError)
  }

  def selectFrom(finder: PathFinder): Def.Initialize[State => Parser[Seq[String]]] =
    Def.setting { state =>
      val names = finder.get.map(_.getName)
      exclusiveParser(names)
    }

}

case class WheatParserError(message: String)
