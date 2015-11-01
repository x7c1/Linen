package x7c1.wheat.build.layout

import sbt._
import x7c1.wheat.build.{ParsedResourceElement, ParsedResource, ResourceNameParser, WheatParserError}

import scala.xml.XML

class LayoutResourceLoader(layoutDir: File){

  def load(fileName: String): Either[WheatParserError, ParsedResource] =
    ResourceNameParser.readPrefix(fileName).right map { prefix =>
      ParsedResource(
        prefix = prefix,
        elements = createElements(fileName, prefix.ofKey))
    }

  private def createElements(fileName: String, prefix: String) = {
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
        ParsedResourceElement(
          key = id, tag = tag,
          label = id.replace(prefix, ""))
    }
  }
}
