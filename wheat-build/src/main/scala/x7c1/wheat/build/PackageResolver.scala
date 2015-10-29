package x7c1.wheat.build

object PackageResolver {
  def toPackage(tag: String) = tag match {
    case "View" => "android.view.View"
    case "TextView" => "android.widget.TextView"
  }
}
