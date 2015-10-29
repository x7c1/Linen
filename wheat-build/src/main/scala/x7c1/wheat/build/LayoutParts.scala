package x7c1.wheat.build

case class LayoutParts(
  targetPackage: String,
  classPrefix: String,
  imports: String,
  fields: String,
  arguments: String,
  assignments: String
)

class LayoutPartsFactory (layout: ParsedLayout){

  def create: LayoutParts = {
    LayoutParts(
      s"package ${layout.targetPackage};",
      layout.prefix, imports, fields, arguments, assignments)
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
  def imports = {
    val xs = toPackage("View") +: layout.elements.map(_.tag).map(toPackage)
    val ys = xs.distinct.map("import " + _ + ";")
    ys.mkString("\n")
  }
  def toPackage(tag: String) = tag match {
    case "View" => "android.view.View"
    case "TextView" => "android.widget.TextView"
  }
}
