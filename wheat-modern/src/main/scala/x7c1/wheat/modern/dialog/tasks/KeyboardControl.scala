package x7c1.wheat.modern.dialog.tasks

import android.content.Context
import android.support.v4.app.DialogFragment
import android.view.View
import android.view.inputmethod.InputMethodManager
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.callback.either.EitherTask
import x7c1.wheat.modern.callback.either.EitherTask.|
import x7c1.wheat.modern.decorator.Imports._

object KeyboardControl {
  def apply[A](dialogFragment: DialogFragment, inputView: View): KeyboardControl[A] = {
    new KeyboardControl(dialogFragment, inputView)
  }
}

class KeyboardControl[A] private(dialogFragment: DialogFragment, inputView: View) {

  private val provide = EitherTask.hold[A]

  def taskToHide(): A | Unit = provide ui {
    Option(dialogFragment.getActivity.getCurrentFocus) match {
      case Some(view) =>
        val manager = dialogFragment.getActivity.
          getSystemService(Context.INPUT_METHOD_SERVICE).
          asInstanceOf[InputMethodManager]

        manager.hideSoftInputFromWindow(
          inputView.getWindowToken,
          InputMethodManager.HIDE_NOT_ALWAYS
        )
        val shown = manager.isAcceptingText
        val msec = if (shown) 300 else 200
        view.runAfter(msec) { _ => dialogFragment.dismiss() }

      case None =>
        Log warn s"[unfocused]"
        dialogFragment.dismiss()
    }
  }
}
