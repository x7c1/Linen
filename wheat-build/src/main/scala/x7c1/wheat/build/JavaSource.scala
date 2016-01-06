package x7c1.wheat.build

import java.io.PrintWriter

import play.twirl.api.TxtFormat
import sbt._

case class JavaSource(
  code: String,
  file: File
)

class JavaSourceFactory [A <: ResourceParts](
  targetDir: File,
  className: String,
  template: A => TxtFormat.Appendable,
  partsFactory: ResourcePartsFactory[A] ){

  def createFrom(resource: ParsedResource): JavaSource = {
    val parts = partsFactory.createFrom(resource)
    JavaSource(
      code = template(parts).body,
      file = targetDir / s"$className.java"
    )
  }
}

trait JavaSourcesFactory {
  def createFrom(resource: ParsedResource): Seq[JavaSource]
}

object JavaSourceWriter {
  def write(source: JavaSource): Unit = {
    val parent = source.file.getParentFile
    if (!parent.exists()){
      parent.mkdirs()
    }
    val writer = new PrintWriter(source.file)
    writer.write(source.code)
    writer.close()
  }

}
