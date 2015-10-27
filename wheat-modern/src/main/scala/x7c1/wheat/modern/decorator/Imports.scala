package x7c1.wheat.modern.decorator

import android.view.View
import android.widget.TextView

object Imports {
  import scala.language.implicitConversions

  implicit def toRichView[A <: View](view: A): RichView[A]
    = new RichView(view)

  implicit def toRichTextView[A <: TextView](view: A): RichTextView[A]
    = new RichTextView(view)
}
