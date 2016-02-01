package x7c1.wheat.modern.decorator

import android.widget.TextView

class RichTextView[A <: TextView](view: A){

  def text: CharSequence = view.getText

  def text_=(value: CharSequence): Unit = view.setText(value)
}
