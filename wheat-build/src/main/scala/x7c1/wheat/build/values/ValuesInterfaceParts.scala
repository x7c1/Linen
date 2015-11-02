package x7c1.wheat.build.values

import x7c1.wheat.build.{ResourceParts, Indent, ParsedResource, ResourcePartsFactory, WheatPackages, ResourcePrefix}

trait ValuesInterfaceParts extends ResourceParts {
  def declarePackage: String
  def prefix: ResourcePrefix
  def methods: String
}

class ValuesInterfacePartsFactory(packages: WheatPackages)
  extends ResourcePartsFactory[ValuesInterfaceParts]{

  override def createFrom(values: ParsedResource): ValuesInterfaceParts = {
    new ValuesInterfacePartsImpl(packages, values)
  }
}

private class ValuesInterfacePartsImpl(packages: WheatPackages, values: ParsedResource)
  extends ValuesInterfaceParts with Indent {

  override def declarePackage: String = s"package ${packages.glueValues};"

  import TypeResolver.toType

  override def methods: String = {
    val x0 = values.elements map { x => s"${toType(x.tag)} ${x.label}();" }
    x0 mkString indent(1)
  }

  override def prefix: ResourcePrefix = values.prefix
}

object TypeResolver {
  def toType(tag: String): String = tag match {
    case "string" => "String"
    case "bool" => "boolean"
  }
}
