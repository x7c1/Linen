package x7c1.wheat.modern.decorator

import android.content.DialogInterface
import android.support.v7.app.AlertDialog
import android.widget.Button

class RichAlertDialog [A <: AlertDialog](view: A){

  def positiveButton: Option[Button] = {
    Option(view getButton DialogInterface.BUTTON_POSITIVE)
  }
  def negativeButton: Option[Button] = {
    Option(view getButton DialogInterface.BUTTON_NEGATIVE)
  }
}
