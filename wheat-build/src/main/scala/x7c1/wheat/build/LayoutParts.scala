package x7c1.wheat.build

case class LayoutParts(
  targetPackage: String,
  classPrefix: String,
  imports: String,
  fields: String,
  arguments: String,
  assignments: String
)

class LayoutPartsFactory (targetPackage: String, layout: ParsedLayout){

  def create: LayoutParts = {
    LayoutParts(
      s"package $targetPackage;",
      layout.classPrefix,
      PackageResolver.importFrom(layout),
      fields, arguments, assignments)
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
  def toPackage(tag: String) = tag match {
    case "View" => "android.view.View"
    case "TextView" => "android.widget.TextView"
  }
}

case class LayoutProviderParts(
  targetPackage: String,
  targetGluePackage: String,
  classPrefix: String,
  rawPrefix: String,
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
      layout.classPrefix,
      layout.rawPrefix,
      s"import $appPackage.R;\n" + PackageResolver.importFrom(layout),
      localVariables,
      assignAtFirst,
      assignCached,
      arguments )
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
    s"view = layoutInflater.inflate(R.layout.${layout.rawPrefix}, parent, attachToRoot);" +
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
  def importFrom(layout: ParsedLayout): String = {
    val xs = toPackage("View") +: layout.elements.map(_.tag).map(toPackage)
    val ys = xs.distinct.map("import " + _ + ";")
    ys.mkString("\n")
  }
  def toPackage(tag: String) = tag match {
    case "View" => "android.view.View"
    case "TextView" => "android.widget.TextView"
  }
}
