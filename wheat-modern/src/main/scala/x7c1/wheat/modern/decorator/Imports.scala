package x7c1.wheat.modern.decorator

import android.support.v7.widget.{LinearLayoutManager, Toolbar, RecyclerView}
import android.view.View
import android.widget.{CompoundButton, TextView}

object Imports {
  import scala.language.implicitConversions

  implicit def toRichCompoundView[A <: CompoundButton](view: A): RichCompoundButton[A]
    = new RichCompoundButton(view)

  implicit def toRichLinearLayoutManager(manager: LinearLayoutManager): RichLinearLayoutManager
    = new RichLinearLayoutManager(manager)

  implicit def toRichView[A <: View](view: A): RichView[A]
    = new RichView(view)

  implicit def toRichTextView[A <: TextView](view: A): RichTextView[A]
    = new RichTextView(view)

  implicit def toRichToolbar[A <: Toolbar](view: A): RichToolbar[A]
    = new RichToolbar(view)

  implicit def toRichRecyclerView[A <: RecyclerView](view: A): RichRecyclerView[A]
    = new RichRecyclerView(view)
}
