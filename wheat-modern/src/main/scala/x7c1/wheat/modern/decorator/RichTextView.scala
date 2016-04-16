package x7c1.wheat.modern.decorator

import android.view.View
import android.widget.TextView

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
}
