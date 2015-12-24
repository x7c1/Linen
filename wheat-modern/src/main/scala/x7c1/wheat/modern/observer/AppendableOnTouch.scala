package x7c1.wheat.modern.observer

import android.view.{MotionEvent, View}
import android.view.View.OnTouchListener

trait AppendableOnTouch extends OnTouchListener { self =>
  def append(listener: OnTouchListener): AppendableOnTouch = {
    new AppendableOnTouch {
      override def onTouch(v: View, event: MotionEvent): Boolean = {
        self.onTouch(v, event) || listener.onTouch(v, event)
      }
    }
  }
}

object AppendableOnTouch {
  def apply(f: (View, MotionEvent) => Boolean): AppendableOnTouch =
    new AppendableOnTouch {
      override def onTouch(v: View, event: MotionEvent): Boolean = {
        f(v, event)
      }
    }

}
