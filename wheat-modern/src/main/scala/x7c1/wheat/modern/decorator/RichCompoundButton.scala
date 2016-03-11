package x7c1.wheat.modern.decorator

import android.widget.CompoundButton
import android.widget.CompoundButton.OnCheckedChangeListener
import x7c1.wheat.modern.decorator.RichCompoundButton.CheckedChangeEvent

class RichCompoundButton[A <: CompoundButton](view: A){

  def checked: Boolean = view.isChecked

  def checked_=(value: Boolean): Unit = view.setChecked(value)

  def onCheckedChanged(f: CheckedChangeEvent[A] => Unit): Unit =
    view setOnCheckedChangeListener new OnCheckedChangeListener {
      override def onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) =
        f(CheckedChangeEvent(view, isChecked))
    }

}

object RichCompoundButton {
  case class CheckedChangeEvent[A <: CompoundButton](
    view: A,
    isChecked: Boolean
  )
}