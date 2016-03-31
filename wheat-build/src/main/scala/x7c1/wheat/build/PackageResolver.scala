package x7c1.wheat.build

object PackageResolver {
  def toPackage(tag: String) = tag match {
    case x if isWidget(x) => s"android.widget.$x"
    case "View" => "android.view.View"
    case x if x.split("\\.").length > 1 => x
    case x =>
      throw new IllegalArgumentException(s"unknown tag: $x")
  }
  private val widget = Seq(
    "Button",
    "EditText",
    "ImageButton",
    "ImageView",
    "LinearLayout",
    "ListView",
    "ProgressBar",
    "RelativeLayout",
    "SeekBar",
    "TextView"
  )
  private def isWidget(tag: String) = widget contains tag
}
