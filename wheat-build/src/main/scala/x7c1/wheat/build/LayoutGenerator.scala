package x7c1.wheat.build

import sbt._
import x7c1.wheat.build.WheatParser.selectFrom

object LayoutGenerator {

  def task: Def.Initialize[InputTask[Unit]] = Def.inputTask {
    val names = selectFrom(layoutDir * "*.xml").parsed

    println("selected files")
    names.map(" * " + _).foreach(println)

    val loader = new ResourceLoader(layoutDir)
    val layouts = names.map(loader.load)
    val sources = layouts.map(_.right.map(applyTemplate))

    println("generated files")
    sources.map(_.right.foreach { _.foreach { source =>
      JavaSourceWriter write source
      println(" * " + source.file.getPath)
    }})
  }

  val packages = new Packages(
    app = "x7c1.linen",
    appLayout = "x7c1.linen.res.layout",
    glueLayout = "x7c1.linen.glue.res.layout"
  )

  val layoutDir = file("linen-starter") / "src/main/res/layout"
  val layoutGenDir = file("linen-glue") / "src/main/java" / "x7c1/linen/glue/res/layout"
  val providerGenDir = file("linen-starter") / "src/main/java" / "x7c1/linen/res/layout"

  def applyTemplate(layout: ParsedResource): Seq[JavaSource] = {
    val layoutSourceFactory = new JavaSourceFactory(
      targetDir = layoutGenDir,
      classSuffix = "Layout",
      template = x7c1.wheat.build.txt.layout.apply,
      partsFactory = new LayoutPartsFactory(packages)
    )
    val providerSourceFactory = new JavaSourceFactory(
      targetDir = providerGenDir,
      classSuffix = "LayoutProvider",
      template = x7c1.wheat.build.txt.layoutProvider.apply,
      partsFactory = new LayoutProviderPartsFactory(packages)
    )
    val factories = Seq(
      layoutSourceFactory,
      providerSourceFactory
    )
    factories.map(_.createFrom(layout))
  }
}

case class Packages(
  app :String,
  appLayout: String,
  glueLayout: String
)

case class ResourcePrefix(
  raw: String,
  ofClass: String,
  ofKey: String
)

case class ParsedResource(
  prefix: ResourcePrefix,
  elements: Seq[ParsedResourceElement]
)

case class ParsedResourceElement(
  key: String,
  label: String,
  tag: String
)
