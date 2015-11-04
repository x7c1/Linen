package x7c1.wheat.build.layout

import x7c1.wheat.build.{ResourcePrefix, ResourceParts}
import x7c1.wheat.build.{ResourcePartsFactory, WheatPackages, ParsedResource, Indent}
import x7c1.wheat.build.PackageResolver.toPackage


trait LayoutProviderParts extends ResourceParts {
  def declarePackage: String
  def prefix: ResourcePrefix
  def imports: String
  def localVariables: String
  def assignAtFirst: String
  def assignCached: String
  def arguments: String
}

class LayoutProviderPartsFactory (packages: WheatPackages)
  extends ResourcePartsFactory[LayoutProviderParts] {

  override def createFrom(layout: ParsedResource): LayoutProviderParts = {
    new LayoutProviderPartsImpl(packages, layout)
  }
}

private class LayoutProviderPartsImpl (packages: WheatPackages, layout: ParsedResource)
  extends LayoutProviderParts with Indent {

  override def declarePackage = s"package ${packages.starterLayout};"

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
      s"import ${packages.starter}.R;",
      s"import ${packages.glueLayout}.${layout.prefix.ofClass}Layout;"
    )
    (x0 ++ x1 ++ x2).distinct mkString "\n"
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
