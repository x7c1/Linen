package x7c1.linen.modern

import android.net.Uri
import x7c1.wheat.modern.action.SiteVisitable

package object struct {

  implicit def toVisitable[A <: UnreadEntry]: SiteVisitable[A] =
    new SiteVisitable[A] {
      override def targetUri(target: A) = Uri parse target.url
    }
}
