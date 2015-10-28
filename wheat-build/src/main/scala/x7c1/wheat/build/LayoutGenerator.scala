package x7c1.wheat.build

import sbt._
import sbt.complete.Parser

object LayoutGenerator {
  import scala.xml.XML

  def task: Def.Initialize[InputTask[Unit]] = Def.inputTask {
    val selected = parser.parsed

    println("selected files")
    selected.foreach(inspect)
  }
  def layoutDir = file("linen-starter") / "src/main/res/layout"

  def targetFileNames = {
    val finder = layoutDir * "*.xml"
    finder.get.map(_.getName)
  }
  def inspect(fileName: String) = {
    val file = layoutDir / fileName
    val xml = XML loadFile file
    val namespace = "http://schemas.android.com/apk/res/android"

    println("(node, id) list!!")

    val pairs = xml.descendant map { node =>
      node.label -> node.attribute(namespace, "id").flatMap(_.headOption)
    } collect {
      case (tag, id) if id.nonEmpty => tag -> id.head.buildString(true)
    }
    pairs.foreach(println)
  }
  lazy val parser: Def.Initialize[State => Parser[Seq[String]]] =
    Def.setting { state => WheatParser.exclusiveParser(targetFileNames) }

}
