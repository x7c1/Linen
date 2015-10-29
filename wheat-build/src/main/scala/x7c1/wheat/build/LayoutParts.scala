package x7c1.wheat.build

import x7c1.wheat.build.PackageResolver.toPackage

case class LayoutParts(
  declarePackage: String,
  prefix: LayoutPrefix,
  imports: String,
  fields: String,
  arguments: String,
  assignments: String
)

class LayoutPartsFactory (targetPackage: String, layout: ParsedLayout) extends Indent {

  def create: LayoutParts = {
    LayoutParts(
      s"package $targetPackage;",
      layout.prefix, imports,
      fields, arguments, assignments)
  }
  def imports = {
    val x0 = Seq("import android.view.View;")
    val x1 = layout.elements.map{ x => s"import ${toPackage(x.tag)};" }
    (x0 ++ x1).distinct mkString "\n"
  }
  def fields = {
    val x0 = Seq("public final View view;")
    val x1 = layout.elements.map { x => s"public final ${x.tag} ${x.label};" }
    (x0 ++ x1) mkString indent(1)
  }
  def arguments = {
    val x0 = Seq("View view")
    val x1 = layout.elements.map{x => s"${x.tag} ${x.label}"}
    (x0 ++ x1) mkString indent(",", 2)
  }
  def assignments = {
    val x0 = Seq("this.view = view;")
    val x1 = layout.elements.map{ x => s"this.${x.label} = ${x.label};"}
    (x0 ++ x1) mkString indent(2)
  }
}

case class LayoutProviderParts(
  declarePackage: String,
  prefix: LayoutPrefix,
  imports: String,
  localVariables: String,
  assignAtFirst: String,
  assignCached: String,
  arguments: String
)

class LayoutProviderPartsFactory (
  appPackage: String, targetPackage: String,
  glueLayoutPackage: String, layout: ParsedLayout ) extends Indent {

  def create: LayoutProviderParts = {
    LayoutProviderParts(
      s"package $targetPackage;",
      layout.prefix, imports,
      localVariables, assignAtFirst, assignCached, arguments
    )
  }
  def imports = {
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
      s"import $appPackage.R;",
      s"import $glueLayoutPackage.${layout.prefix.ofClass}Layout;"
    )
    (x0 ++ x1 ++  x2).distinct mkString "\n"
  }
  def localVariables = {
    val x0 = Seq("final View view;")
    val x1 = layout.elements.map{ x => s"final ${x.tag} ${x.label};" }
    (x0 ++ x1) mkString indent(2)
  }
  def assignAtFirst = {
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
  def assignCached = {
    val x0 = Seq(
      "view = convertView;"
    )
    val x1 = layout.elements.map{ x =>
      s"${x.label} = (${x.tag}) view.getTag(R.id.${x.key});"
    }
    (x0 ++ x1) mkString indent(3)
  }
  def arguments = {
    val x0 = Seq("view")
    val x1 = layout.elements.map(_.label)
    (x0 ++ x1) mkString indent(",", 3)
  }
}

trait Indent {
  def indent(n: Int) = "\n" + ("    " * n)
  def indent(tail: String, n: Int) = tail + "\n" + ("    " * n)
}
