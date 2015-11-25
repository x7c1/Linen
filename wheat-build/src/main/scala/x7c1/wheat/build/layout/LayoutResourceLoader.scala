package x7c1.wheat.build.layout

import sbt._
import x7c1.wheat.build.WheatParser.camelizeTail
import x7c1.wheat.build.{WheatParserError, ParsedResourceElement, ResourceElementsLoader, ResourceLoader}

import scala.xml.{Elem, XML}

class LayoutResourceLoader(dir: File) extends ResourceLoader {
  override def load(fileName: String) = {
    val loader = ResourceLoader apply new LayoutElementsLoader(dir, fileName)
    loader load fileName
  }
}

class LayoutElementsLoader(dir: File, fileName: String) extends ResourceElementsLoader {
  override def create(prefix: String) = {
    val file = dir / fileName
    val xml = XML loadFile file
    val namespace = "http://schemas.android.com/apk/res/android"

    val elements = xml.descendant map { node =>
      node -> node.attribute(namespace, "id").flatMap(_.headOption)
    } collect {
      case (node, Some(attr)) =>
        node.label -> attr.buildString(true).replace("@+id/", "")
    } collect {
      case (tag, id) if id startsWith prefix =>
        camelizeTail(id.replace(prefix, "")).right.map{ label =>
          ParsedResourceElement(key = id, tag = tag, label = label)
        }
    }
    elements ++ include(xml)
  }
  private def include(xml: Elem): List[Either[WheatParserError, ParsedResourceElement]] =
    xml.descendant collect {
      case node if node.label == "include" =>
        node.attribute("layout").flatMap(_.headOption)
    } collect {
      case Some(attr) =>
        attr.buildString(true).replace("@layout/", "")
    } map { name =>
      new LayoutResourceLoader(dir) load s"$name.xml"
    } flatMap {
      case Right(resource) =>
        resource.elements map Right.apply
      case Left(errors) =>
        errors map Left.apply
    }
}
