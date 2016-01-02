package x7c1.wheat.build.layout

import x7c1.wheat.build.{Indent, ParsedResource, ResourceParts, ResourcePartsFactory, ResourcePrefix, WheatPackages}

trait ViewHolderParentParts extends ResourceParts {
  def declarePackage: String
  def imports: String
}

class ViewHolderParentPartsFactory (packages: WheatPackages)
  extends ResourcePartsFactory[ViewHolderParentParts] {

  override def createFrom(resource: ParsedResource): ViewHolderParentParts = {
    new ViewHolderParentPartsImpl(packages, resource)
  }
}

private class ViewHolderParentPartsImpl(packages: WheatPackages, layout: ParsedResource)
  extends ViewHolderParentParts with Indent {

  override def declarePackage: String = s"package ${packages.glueLayout}"

  override def imports: String = {
    val x0 = Seq(
      "import android.support.v7.widget.RecyclerView;",
      "import android.view.View;"
    )
    x0.distinct mkString "\n"
  }

  override def prefix: ResourcePrefix = layout.prefix
}
