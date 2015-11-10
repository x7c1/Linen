package x7c1.wheat.build.layout

import x7c1.wheat.build.PackageResolver.toPackage
import x7c1.wheat.build.{Indent, ParsedResource, ResourceParts, ResourcePartsFactory, WheatPackages}

trait ViewHolderProviderParts extends ResourceParts {
  def declarePackage: String
  def imports: String
  def arguments: String
}

class ViewHolderProviderPartsFactory(packages: WheatPackages)
  extends ResourcePartsFactory[ViewHolderProviderParts]{

  override def createFrom(resource: ParsedResource): ViewHolderProviderParts =
    new ViewHolderProviderPartsImpl(packages, resource)
}

private class ViewHolderProviderPartsImpl(
  packages: WheatPackages,
  layout: ParsedResource) extends ViewHolderProviderParts with Indent {

  override def declarePackage = s"package ${packages.starterLayout};"

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
      "import x7c1.wheat.ancient.resource.ViewHolderProvider;",
      s"import ${packages.starter}.R;",
      s"import ${packages.glueLayout}.${layout.prefix.ofClass};"
    )
    (x0 ++ x1 ++ x2).distinct mkString "\n"
  }

  override def arguments = {
    val x0 = "view"
    val x1 = layout.elements map { x =>
      s"(${x.tag}) view.findViewById(R.id.${x.key})"
    }
    (x0 +: x1) mkString indent(",", 3)
  }

  override def prefix = layout.prefix
}
