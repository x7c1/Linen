package x7c1.wheat.build

case class LayoutParts(
  declarePackage: String,
  prefix: LayoutPrefix,
  imports: String,
  fields: String,
  arguments: String,
  assignments: String
)

class LayoutPartsFactory (targetPackage: String, layout: ParsedLayout){

  def create: LayoutParts = {
    LayoutParts(
      s"package $targetPackage;",
      layout.prefix,
      imports,
      fields, arguments, assignments)
  }
  def imports = {
    PackageResolver.imports(layout).mkString("\n")
  }
  def fields = {
    val make = (tag: String, label: String) => s"public final $tag $label;"
    val xs = make("View", "view") +: layout.elements.map {
      x => make(x.tag, x.label)
    }
    xs.mkString("\n    ")
  }
  def arguments = {
    val xs = "View view" +: layout.elements.map{x => s"${x.tag} ${x.label}"}
    xs.mkString(",\n        ")
  }
  def assignments = {
    def make = (label: String) => s"this.$label = $label;"
    val ys = make("view") +: layout.elements.map{_.label}.map(make)
    ys.mkString("\n        ")
  }
}

case class LayoutProviderParts(
  declarePackage: String,
  targetGluePackage: String,
  prefix: LayoutPrefix,
  imports: String,
  localVariables: String,
  assignAtFirst: String,
  assignCached: String,
  arguments: String
)

class LayoutProviderPartsFactory (
  appPackage: String, targetPackage: String, targetGluePackage: String, layout: ParsedLayout)
{
  def create: LayoutProviderParts = {
    LayoutProviderParts(
      s"package $targetPackage;",
      targetGluePackage,
      layout.prefix,
      imports,
      localVariables,
      assignAtFirst,
      assignCached,
      arguments )
  }
  def imports = {
    val xs = Seq(
      "import android.content.Context;",
      "import android.view.LayoutInflater;",
      "import android.view.ViewGroup;"
    )
    val ys = Seq(
      "import x7c1.wheat.ancient.resource.LayoutProvider;",
      s"import $appPackage.R;",
      s"import $targetGluePackage.${layout.prefix.ofClass}Layout;"
    )
    (xs ++ PackageResolver.imports(layout) ++  ys).distinct.mkString("\n")
  }
  def localVariables = {
    val make = (tag: String, label: String) => s"final $tag $label;"
    val xs = make("View", "view") +: layout.elements.map{x => s"final ${x.tag} ${x.label};"}
    xs.mkString("\n        ")
  }
  def assignAtFirst = {
    val xs = layout.elements.map{ x =>
      s"${x.label} = (${x.tag}) view.findViewById(R.id.${x.key});"
    }
    val ys = layout.elements.map{ x =>
      s"view.setTag(R.id.${x.key}, ${x.label});"
    }
    s"view = layoutInflater.inflate(R.layout.${layout.prefix.raw}, parent, attachToRoot);" +
      "\n            " + xs.mkString("\n            ") +
      "\n            " + ys.mkString("\n            ")
  }
  def assignCached = {
    val xs = layout.elements.map{ x =>
      s"${x.label} = (${x.tag}) view.getTag(R.id.${x.key});"
    }
    "view = convertView;" + "\n            " +
      xs.mkString("\n            ")
  }
  def arguments = {
    val xs = "view" +: layout.elements.map{_.label}
    xs.mkString(",\n            ")
  }
}

object PackageResolver {
  def imports(layout: ParsedLayout): Seq[String] = {
    val xs = toPackage("View") +: layout.elements.map(_.tag).map(toPackage)
    xs.distinct.map("import " + _ + ";")
  }
  def toPackage(tag: String) = tag match {
    case "View" => "android.view.View"
    case "TextView" => "android.widget.TextView"
  }
}
