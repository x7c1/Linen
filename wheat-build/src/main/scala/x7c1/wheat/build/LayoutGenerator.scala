package x7c1.wheat.build

import java.io.PrintWriter

import sbt._
import sbt.complete.Parser

object LayoutGenerator {
  import scala.xml.XML

  def task: Def.Initialize[InputTask[Unit]] = Def.inputTask {
    val selected = parser.parsed

    println("selected files")
    selected.map(" * " + _).foreach(println)

    val layouts = selected.map(inspect)
    val sources = layouts.map(_.right.map(applyTemplate))

    println("generated files")
    sources.map(_.right.foreach { _.foreach { source =>
      writeSource(source)
      println(" * " + source.file.getPath)
    }})
  }
  val targetGluePackage = "x7c1.linen.glue.res.layout"
  val targetAppPackage = "x7c1.linen.res.layout"
  val appPackage = "x7c1.linen"

  def layoutDir = file("linen-starter") / "src/main/res/layout"

  def layoutGenDir = file("linen-glue") / "src/main/java" / "x7c1/linen/glue/res/layout"
  def providerGenDir = file("linen-starter") / "src/main/java" / "x7c1/linen/res/layout"

  def inspect(fileName: String): Either[WheatParserError, ParsedLayout] = {
    LayoutNameParser.readPrefix(fileName).right map { prefix =>
      ParsedLayout(
        rawPrefix = prefix.rawPrefix,
        classPrefix = prefix.classPrefix,
        elements = createElements(fileName, prefix.keyPrefix))
    }
  }

  def writeSource(source: JavaSource): Unit = {
    val parent = source.file.getParentFile
    if (!parent.exists()){
      parent.mkdirs()
    }
    val writer = new PrintWriter(source.file)
    writer.write(source.code)
    writer.close()
  }

  def applyTemplate(layout: ParsedLayout): Seq[JavaSource] = {
    val forLayout = {
      val parts = new LayoutPartsFactory(targetGluePackage, layout).create
      val code = x7c1.wheat.build.txt.layout(parts).body
      val path = layoutGenDir / s"${parts.classPrefix}Layout.java"
      JavaSource(code, path)
    }
    val forLayoutProvider = {
      val factory = new LayoutProviderPartsFactory(
        appPackage, targetAppPackage, targetGluePackage, layout
      )
      val parts = factory.create
      val code = x7c1.wheat.build.txt.layoutProvider(parts).body

      val path = providerGenDir / s"${parts.classPrefix}LayoutProvider.java"
      JavaSource(code, path)
    }
    Seq(forLayout, forLayoutProvider)
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

case class LayoutPrefix(
  rawPrefix: String,
  classPrefix: String,
  keyPrefix: String)

case class ParsedLayout(
  rawPrefix: String,
  classPrefix: String,
  elements: Seq[ParsedLayoutElement])

case class ParsedLayoutElement(key: String, label: String, tag: String)

case class JavaSource(
  code: String,
  file: File
)

object LayoutNameParser {
  import sbt.complete.DefaultParsers._

  def readPrefix(name: String): Either[WheatParserError, LayoutPrefix] = {
    parse(name, parserToPrefix).left.map(WheatParserError).joinRight
  }

  private def parserToPrefix =
    any.+.string <~ token(".xml") map {
      prefix => WheatParser.toCamelCase(prefix).right map {
        camel => LayoutPrefix(prefix, camel, prefix + "__")
      }
    }
}
