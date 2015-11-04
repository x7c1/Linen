package x7c1.wheat.build.values

import sbt._
import x7c1.wheat.build.WheatParser.camelizeTail
import x7c1.wheat.build.{ParsedResourceElement, ResourceElementsLoader, ResourceLoader}

import scala.xml.XML

class ValuesResourceLoader(dir: File) extends ResourceLoader {
  override def load(fileName: String) = {
    val loader = ResourceLoader apply new ValuesElementsLoader(dir, fileName)
    loader load fileName
  }
}

class ValuesElementsLoader (dir: File, fileName: String) extends ResourceElementsLoader {
  override def create(prefix: String) = {
    val file = dir / fileName
    val xml = XML loadFile file

    xml.descendant map { node =>
      node -> node.attribute("name").flatMap(_.headOption)
    } collect {
      case (node, Some(attr)) =>
        node.label -> attr.buildString(true)
    } collect {
      case (tag, name) if name startsWith prefix =>
        camelizeTail(name.replace(prefix, "")).right.map{ label =>
          ParsedResourceElement(key = name, tag = tag, label = label)
        }
    }
  }
}
