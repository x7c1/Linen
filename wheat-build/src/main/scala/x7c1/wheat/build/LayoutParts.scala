package x7c1.wheat.build

import x7c1.wheat.build.PackageResolver.toPackage


trait ResourceParts {
  def prefix: ResourcePrefix
}
trait ResourcePartsFactory [A <: ResourceParts]{
  def createFrom(layout: ParsedResource): A
}

trait LayoutParts extends ResourceParts {
  def declarePackage: String
  def prefix: ResourcePrefix
  def imports: String
  def fields: String
  def arguments: String
  def assignments: String
}

class LayoutPartsFactory (packages: Packages)
  extends ResourcePartsFactory[LayoutParts] {

  override def createFrom(layout: ParsedResource): LayoutParts = {
    new LayoutPartsImpl(packages, layout)
  }
}

class LayoutPartsImpl (packages: Packages, layout: ParsedResource)
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

trait LayoutProviderParts extends ResourceParts {
  def declarePackage: String
  def prefix: ResourcePrefix
  def imports: String
  def localVariables: String
  def assignAtFirst: String
  def assignCached: String
  def arguments: String
}

class LayoutProviderPartsFactory (packages: Packages)
  extends ResourcePartsFactory[LayoutProviderParts] {

  override def createFrom(layout: ParsedResource): LayoutProviderParts = {
    new LayoutProviderPartsImpl(packages, layout)
  }
}

class LayoutProviderPartsImpl (packages: Packages, layout: ParsedResource)
  extends LayoutProviderParts with Indent {

  override def declarePackage = s"package ${packages.appLayout};"

  override def prefix = layout.prefix

  override def imports = {
    val x0 = Seq(
      "import android.content.Context;",
      "import android.view.LayoutInflater;",
      "import android.view.ViewGroup;",
      "import android.view.View;"
    )
    val x1 = layout.elements.map{ x =>
      s"import ${toPackage(x.tag)};"
    }
    val x2 = Seq(
      "import x7c1.wheat.ancient.resource.LayoutProvider;",
      s"import ${packages.app}.R;",
      s"import ${packages.glueLayout}.${layout.prefix.ofClass}Layout;"
    )
    (x0 ++ x1 ++  x2).distinct mkString "\n"
  }
  override def localVariables = {
    val x0 = Seq("final View view;")
    val x1 = layout.elements.map{ x => s"final ${x.tag} ${x.label};" }
    (x0 ++ x1) mkString indent(2)
  }
  override def assignAtFirst = {
    val x0 = Seq(
      s"view = layoutInflater.inflate(R.layout.${layout.prefix.raw}, parent, attachToRoot);"
    )
    val x1 = layout.elements.map{ x =>
      s"${x.label} = (${x.tag}) view.findViewById(R.id.${x.key});"
    }
    val x2 = layout.elements.map{ x =>
      s"view.setTag(R.id.${x.key}, ${x.label});"
    }
    (x0 ++ x1 ++ x2) mkString indent(3)
  }
  override def assignCached = {
    val x0 = Seq(
      "view = convertView;"
    )
    val x1 = layout.elements.map{ x =>
      s"${x.label} = (${x.tag}) view.getTag(R.id.${x.key});"
    }
    (x0 ++ x1) mkString indent(3)
  }
  override def arguments = {
    val x0 = Seq("view")
    val x1 = layout.elements.map(_.label)
    (x0 ++ x1) mkString indent(",", 3)
  }
}

trait Indent {
  def indent(n: Int) = "\n" + ("    " * n)
  def indent(tail: String, n: Int) = tail + "\n" + ("    " * n)
}
