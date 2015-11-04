package x7c1.wheat.build.values

import x7c1.wheat.build.values.MethodResolver.toMethod
import x7c1.wheat.build.values.TypeResolver.toType
import x7c1.wheat.build.{ParsedResourceElement, ResourcePrefix, Indent, ParsedResource, ResourcePartsFactory, WheatPackages, ResourceParts}

trait ValuesProviderParts extends ResourceParts {
  def declarePackage: String
  def imports: String
  def arguments: String

  def fields: String
  def parameters: String
  def assignPrivate: String
  def methods: String
}

class ValuesProviderPartsFactory(packages: WheatPackages)
  extends ResourcePartsFactory[ValuesProviderParts]{

  override def createFrom(resource: ParsedResource): ValuesProviderParts = {
    new ValuesProviderPartsImpl(packages, resource)
  }
}

private class ValuesProviderPartsImpl(packages: WheatPackages, values: ParsedResource)
  extends ValuesProviderParts with Indent {

  override def prefix: ResourcePrefix = values.prefix

  override def declarePackage = s"package ${packages.starterValues};"

  override def imports = {
    val x0 = Seq(
      "import android.content.Context;",
      "import x7c1.wheat.ancient.resource.ValuesProvider;",
      s"import ${packages.starter}.R;",
      s"import ${packages.glueValues}.${values.prefix.ofClass}Values;"
    )
    x0 mkString "\n"
  }
  override def arguments: String = {
    val x0 = "context"
    val x1 = values.elements map { x => s"R.${x.tag}.${x.key}" }
    (x0 +: x1) mkString indent(",", 3)
  }

  override def fields: String = {
    val x0 = Seq(
      "private final Context context;"
    )
    val x1 = values.elements map { x =>
      s"private final int ${x.label}Id;"
    }
    val x2 = values.elements map { x =>
      s"private ${toType(x.tag)} ${x.label};"
    }
    (x0 ++ x1 ++ x2) mkString indent(2)
  }

  override def parameters = {
    val x0 = "Context context"
    val x1 = values.elements map { x => s"int ${x.label}" }
    (x0 +: x1) mkString indent(",", 3)
  }

  override def assignPrivate: String = {
    val x0 = "this.context = context;"
    val x1 = values.elements map { x => s"this.${x.label}Id = ${x.label};" }
    (x0 +: x1) mkString indent(3)
  }

  override def methods: String = {
    def format(x: ParsedResourceElement) =
      s"""@Override
        |public ${toType(x.tag)} ${x.label}(){
        |    if (${x.label} == null){
        |        ${x.label} = context.getResources().${toMethod(x.tag)}(${x.label}Id);
        |    }
        |    return ${x.label};
        |}""".stripMargin

    values.elements.
      map(format).
      mkString("\n").
      replaceAll("\n", indent(2))
  }
}

object MethodResolver {
  def toMethod(tag: String): String = tag match {
    case "string" => "getString"
    case "bool" => "getBoolean"
  }
}
