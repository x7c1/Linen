package x7c1.wheat.build

object PackageResolver {
  def toPackage(tag: String) = tag match {
    case "Button" => "android.widget.Button"
    case "ImageView" => "android.widget.ImageView"
    case "LinearLayout" => "android.widget.LinearLayout"
    case "ListView" => "android.widget.ListView"
    case "ProgressBar" => "android.widget.ProgressBar"
    case "RelativeLayout" => "android.widget.RelativeLayout"
    case "SeekBar" => "android.widget.SeekBar"
    case "TextView" => "android.widget.TextView"
    case "View" => "android.view.View"
    case x if x.split("\\.").length > 1 => x
  }
}
