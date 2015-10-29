package x7c1.wheat.build

import sbt._
import sbt.complete.Parser

object LayoutGenerator {
  import scala.xml.XML

  def task: Def.Initialize[InputTask[Unit]] = Def.inputTask {
    val selected = parser.parsed

    println("selected files")

    val list = selected.map(inspect)

    val sources = list.map(_.right.map(applyTemplate))
    sources.foreach(println)

  }
  val targetPackage = "x7c1.linen.glue.res.layout"

  def layoutDir = file("linen-starter") / "src/main/res/layout"

  def inspect(fileName: String): Either[WheatParserError, ParsedLayout] = {
    LayoutNameParser.readPrefix(fileName).right map { prefix =>
      ParsedLayout(
        targetPackage = targetPackage,
        prefix = prefix.camel,
        elements = createElements(fileName, prefix.key))
    }
  }

  def applyTemplate(layout: ParsedLayout): String = {
    val parts = new LayoutPartsFactory(layout).create
    x7c1.wheat.build.txt.layout(parts).body
  }
  def createElements(fileName: String, prefix: String) = {
    val file = layoutDir / fileName
    val xml = XML loadFile file
    val namespace = "http://schemas.android.com/apk/res/android"

    xml.descendant map { node =>
      node -> node.attribute(namespace, "id").flatMap(_.headOption)
    } collect {
      case (node, Some(attr)) =>
        node.label -> attr.buildString(true).replace("@+id/", "")
    } collect {
      case (tag, id) if id startsWith prefix =>
        ParsedLayoutElement(
          key = id, tag = tag,
          label = id.replace(prefix, ""))
    }
  }

  lazy val parser: Def.Initialize[State => Parser[Seq[String]]] =
    Def.setting { state =>
      val finder = layoutDir * "*.xml"
      val names = finder.get.map(_.getName)
      WheatParser.exclusiveParser(names)
    }
}

case class LayoutPrefix(camel: String, key: String)

case class ParsedLayout(
  targetPackage: String,
  prefix: String,
  elements: Seq[ParsedLayoutElement])

case class ParsedLayoutElement(key: String, label: String, tag: String)

object LayoutNameParser {
  import sbt.complete.DefaultParsers._

  def readPrefix(name: String): Either[WheatParserError, LayoutPrefix] = {
    parse(name, parserToPrefix).left.map(WheatParserError).joinRight
  }

  private def parserToPrefix =
    any.+.string <~ token(".xml") map {
      prefix => WheatParser.toCamelCase(prefix).right map {
        camel => LayoutPrefix(camel, prefix + "__")
      }
    }
}
