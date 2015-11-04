package x7c1.wheat.build.layout

import x7c1.wheat.build.PackageResolver.toPackage
import x7c1.wheat.build.{Indent, ParsedResource, ResourceParts, ResourcePartsFactory, ResourcePrefix, WheatPackages}


trait LayoutParts extends ResourceParts {
  def declarePackage: String
  def prefix: ResourcePrefix
  def imports: String
  def fields: String
  def arguments: String
  def assignments: String
}

class LayoutPartsFactory (packages: WheatPackages)
  extends ResourcePartsFactory[LayoutParts] {

  override def createFrom(layout: ParsedResource): LayoutParts = {
    new LayoutPartsImpl(packages, layout)
  }
}

private class LayoutPartsImpl (packages: WheatPackages, layout: ParsedResource)
  extends LayoutParts with Indent {

  override def declarePackage = s"package ${packages.glueLayout};"

  override def prefix = layout.prefix

  override def imports = {
    val x0 = Seq("import android.view.View;")
    val x1 = layout.elements.map{ x => s"import ${toPackage(x.tag)};" }
    (x0 ++ x1).distinct mkString "\n"
  }
  override def fields = {
    val x0 = Seq("public final View view;")
    val x1 = layout.elements.map { x => s"public final ${x.tag} ${x.label};" }
    (x0 ++ x1) mkString indent(1)
  }
  override def arguments = {
    val x0 = Seq("View view")
    val x1 = layout.elements.map{x => s"${x.tag} ${x.label}"}
    (x0 ++ x1) mkString indent(",", 2)
  }
  override def assignments = {
    val x0 = Seq("this.view = view;")
    val x1 = layout.elements.map{ x => s"this.${x.label} = ${x.label};"}
    (x0 ++ x1) mkString indent(2)
  }
}
