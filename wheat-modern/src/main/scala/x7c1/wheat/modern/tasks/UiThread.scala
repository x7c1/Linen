package x7c1.wheat.modern.tasks

import android.view.View
import x7c1.wheat.modern.callback.CallbackTask
import x7c1.wheat.modern.decorator.Imports._

class UiThread[A <: View](view: A){

  def apply[B](procedure: A => B): CallbackTask[B] = {
    val f: (B => Unit) => Unit = { onFinish =>
      view runUi { _ => onFinish(procedure(view)) }
    }
    CallbackTask(f)
  }
}

object UiThread {
  def via[A <: View](view: A): UiThread[A] = new UiThread(view)
}