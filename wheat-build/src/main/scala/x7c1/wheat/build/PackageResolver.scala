package x7c1.wheat.build

object PackageResolver {
  def toPackage(tag: String) = tag match {
    case "LinearLayout" => "android.widget.LinearLayout"
    case "ListView" => "android.widget.ListView"
    case "TextView" => "android.widget.TextView"
    case "View" => "android.view.View"
    case x if x.split("\\.").nonEmpty => x
  }
}
