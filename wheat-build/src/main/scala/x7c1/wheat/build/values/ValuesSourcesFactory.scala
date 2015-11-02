package x7c1.wheat.build.values

import x7c1.wheat.build.{JavaSourceFactory, JavaSource, ParsedResource}

class ValuesSourcesFactory(locations: ValuesLocations){
  def createFrom(values: ParsedResource): Seq[JavaSource] = {
    val valuesSourceFactory = new JavaSourceFactory(
      targetDir = locations.valuesDst,
      classSuffix = "Values",
      template = x7c1.wheat.build.txt.values.apply,
      partsFactory = new ValuesInterfacePartsFactory(locations.packages)
    )
    val providerSourceFactory = new JavaSourceFactory(
      targetDir = locations.providerDst,
      classSuffix = "ValuesProvider",
      template = x7c1.wheat.build.txt.valuesProvider.apply,
      partsFactory = new ValuesProviderPartsFactory(locations.packages)
    )
    val factories = Seq(
      valuesSourceFactory,
      providerSourceFactory
    )
    factories.map(_.createFrom(values))
  }
}
