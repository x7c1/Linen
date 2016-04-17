package x7c1.wheat.modern.decorator

import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.TextView
import x7c1.wheat.modern.resource.EmptyDrawableGetter

class RichTextView[A <: TextView](view: A){

  def text: CharSequence = view.getText

  def text_=(value: CharSequence): Unit = view.setText(value)

  def toggleVisibility(x: CharSequence): Unit = {
    view setVisibility {
      x.length match {
        case 0 => View.GONE
        case _ => View.VISIBLE
      }
    }
    view setText x
  }

  def setHtml(source: String): Unit = {
    val html = Html.fromHtml(
      source,
      EmptyDrawableGetter/* imageGetter */,
      null/* tagHandler */
    )
    view setText html
    view setMovementMethod LinkMovementMethod.getInstance()
  }
}
