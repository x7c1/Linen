package x7c1.wheat.build.layout

import x7c1.wheat.build.{JavaSourceFactory, ParsedResource, JavaSourcesFactory}

class ViewHolderSourcesFactory(locations: LayoutLocations) extends JavaSourcesFactory {
  override def createFrom(resource: ParsedResource) = {
    val holderSourceFactory = new JavaSourceFactory(
      targetDir = locations.layoutDst,
      className = resource.prefix.ofClass,
      template = x7c1.wheat.build.txt.viewHolder.apply,
      partsFactory = new ViewHolderPartsFactory(locations.packages)
    )
    val providerSourceFactory = new JavaSourceFactory(
      targetDir = locations.providerDst,
      className = resource.prefix.ofClass + "Provider",
      template = x7c1.wheat.build.txt.viewHolderProvider.apply,
      partsFactory = new ViewHolderProviderPartsFactory(locations.packages)
    )
    val forParent = resource.prefix.parentClassName match {
      case Some(parentClassName) => Seq apply new JavaSourceFactory(
        targetDir = locations.layoutDst,
        className = parentClassName,
        template = x7c1.wheat.build.txt.viewHolderParent.apply,
        partsFactory = new ViewHolderParentPartsFactory(locations.packages)
      )
      case None => Seq()
    }
    val factories = forParent ++ Seq(
      holderSourceFactory,
      providerSourceFactory
    )
    factories.map(_ createFrom resource)
  }
}
