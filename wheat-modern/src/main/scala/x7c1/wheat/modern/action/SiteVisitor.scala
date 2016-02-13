package x7c1.wheat.modern.action

import android.content.{Context, Intent}
import android.net.Uri

object SiteVisitor {
  def apply(context: Context): SiteVisitor = new SiteVisitor {
    override def open[A: SiteVisitable](target: A): Unit = {
      val intent = new Intent(Intent.ACTION_VIEW)
      intent setData implicitly[SiteVisitable[A]].targetUri(target)
      context startActivity intent
    }
  }
}

trait SiteVisitor {
  def open[A: SiteVisitable](target: A): Unit
}

trait SiteVisitable[A] {
  def targetUri(target: A): Uri
}
