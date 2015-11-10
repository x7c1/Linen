package x7c1.wheat.build.layout

import x7c1.wheat.build.{JavaSourceFactory, ParsedResource, JavaSourcesFactory}

class ViewHolderSourcesFactory(locations: LayoutLocations) extends JavaSourcesFactory {
  override def createFrom(resource: ParsedResource) = {
    val holderSourceFactory = new JavaSourceFactory(
      targetDir = locations.layoutDst,
      classSuffix = "",
      template = x7c1.wheat.build.txt.viewHolder.apply,
      partsFactory = new ViewHolderPartsFactory(locations.packages)
    )
    val factories = Seq(
      holderSourceFactory
    )
    factories.map(_ createFrom resource)
  }
}
