package x7c1.wheat.modern.callback

import android.view.View
import x7c1.wheat.modern.decorator.Imports._

class UiThreadTask[A <: View](view: A){

  def apply[B](procedure: A => B): CallbackTask[B] = {
    val f: (B => Unit) => Unit = { onFinish =>
      view runUi { _ => onFinish(procedure(view)) }
    }
    CallbackTask(f)
  }
}

object UiThreadTask {
  def from[A <: View](view: A): UiThreadTask[A] = new UiThreadTask(view)
}
