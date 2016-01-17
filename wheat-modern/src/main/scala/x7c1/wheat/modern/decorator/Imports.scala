package x7c1.wheat.modern.decorator

import android.support.v7.widget.{Toolbar, RecyclerView}
import android.view.View
import android.widget.TextView

object Imports {
  import scala.language.implicitConversions

  implicit def toRichView[A <: View](view: A): RichView[A]
    = new RichView(view)

  implicit def toRichTextView[A <: TextView](view: A): RichTextView[A]
    = new RichTextView(view)

  implicit def toRichToolbar[A <: Toolbar](view: A): RichToolbar[A]
    = new RichToolbar(view)

  implicit def toRichRecyclerView[A <: RecyclerView](view: A): RichRecyclerView[A]
    = new RichRecyclerView(view)
}
