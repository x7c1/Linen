package x7c1.wheat.modern.decorator

import android.content.Context
import android.support.v7.app.AlertDialog
import android.support.v7.widget.{LinearLayoutManager, RecyclerView, Toolbar}
import android.view.View
import android.widget.{CompoundButton, TextView}

object Imports {
  import scala.language.implicitConversions

  implicit def toRichContext[A <: Context](context: A): RichContext[A]
    = new RichContext(context)

  implicit def toRichCompoundView[A <: CompoundButton](view: A): RichCompoundButton[A]
    = new RichCompoundButton(view)

  implicit def toRichAlertDialog[A <: AlertDialog](view: A): RichAlertDialog[A]
    = new RichAlertDialog(view)

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
