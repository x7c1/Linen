package x7c1.wheat.build.layout

import sbt._
import x7c1.wheat.build.{ParsedResourceElement, ResourceElementsLoader, ResourceLoader}

import scala.xml.XML

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

    xml.descendant map { node =>
      node -> node.attribute(namespace, "id").flatMap(_.headOption)
    } collect {
      case (node, Some(attr)) =>
        node.label -> attr.buildString(true).replace("@+id/", "")
    } collect {
      case (tag, id) if id startsWith prefix =>
        camelCase(id.replace(prefix, "")).right.map{ label =>
          ParsedResourceElement(key = id, tag = tag, label = label)
        }
    }
  }
}
