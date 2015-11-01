package x7c1.wheat.build.layout

import x7c1.wheat.build.{ParsedResource, JavaSource, JavaSourceFactory}

class JavaLayoutSourcesFactory(locations: LayoutLocations){
  def createFrom(layout: ParsedResource): Seq[JavaSource] = {
    val layoutSourceFactory = new JavaSourceFactory(
      targetDir = locations.layoutDst,
      classSuffix = "Layout",
      template = x7c1.wheat.build.txt.layout.apply,
      partsFactory = new LayoutPartsFactory(locations.packages)
    )
    val providerSourceFactory = new JavaSourceFactory(
      targetDir = locations.providerDst,
      classSuffix = "LayoutProvider",
      template = x7c1.wheat.build.txt.layoutProvider.apply,
      partsFactory = new LayoutProviderPartsFactory(locations.packages)
    )
    val factories = Seq(
      layoutSourceFactory,
      providerSourceFactory
    )
    factories.map(_.createFrom(layout))
  }
}
