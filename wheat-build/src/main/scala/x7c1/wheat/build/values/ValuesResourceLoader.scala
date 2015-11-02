package x7c1.wheat.build.values

import sbt._
import sbt.complete.DefaultParsers._
import x7c1.wheat.build.{ParsedResource, ParsedResourceElement, ResourceNameParser, WheatParserError}

import scala.xml.XML

class ValuesResourceLoader(valuesDir: File){

  def load(fileName: String): Either[Seq[WheatParserError], ParsedResource] = {
    ResourceNameParser.readPrefix(fileName) match {
      case Right(prefix) =>
        val (l, r) = createElements(fileName, prefix.ofKey).partition(_.isLeft)
        val errors = l.map(_.left.get)
        if (errors.isEmpty) {
          Right apply ParsedResource(prefix = prefix, elements = r.map(_.right.get))
        } else {
          Left apply errors
        }
      case Left(error) => Left(Seq(error))
    }
  }
  private def createElements(fileName: String, prefix: String) = {
    val file = valuesDir / fileName
    val xml = XML loadFile file

    xml.descendant map { node =>
      node -> node.attribute("name").flatMap(_.headOption)
    } collect {
      case (node, Some(attr)) =>
        node.label -> attr.buildString(true)
    } collect {
      case (tag, name) if name startsWith prefix =>
        camelCase(name.replace(prefix, "")).right.map{ label =>
          ParsedResourceElement(key = name, tag = tag, label = label)
        }
    }
  }

  def camelCase(string: String): Either[WheatParserError, String] = {
    val alphabet = token('a' to 'z') | token('A' to 'Z')
    val parser = (alphabet.+.string ~ (token('_') ~> alphabet.+.string).+) map {
      case (head, tail) => head + tail.map(_.capitalize).mkString
    }
    parse(string, parser).left.map(WheatParserError)
  }

}
