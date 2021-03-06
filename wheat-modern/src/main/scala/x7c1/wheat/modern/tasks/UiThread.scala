package x7c1.wheat.modern.tasks

import android.os.{Looper, Handler}
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
  def join[B](task: CallbackTask[B]): CallbackTask[B] = {
    apply(_ => ()) flatMap (_ => task)
  }
}

object UiThread {

  def run[A](f: => A): Unit = post(f)

  def runDelayed[A](msec: Long)(f: => A): Unit =
    new Handler(Looper.getMainLooper).postDelayed(runnable(f), msec)

  def main[A](f: => A): CallbackTask[A] = CallbackTask { g =>
    post(g(f))
  }
  def via[A <: View](view: A): UiThread[A] = new UiThread(view)

  private def post[A](f: => A) =
    new Handler(Looper.getMainLooper) post runnable(f)

  private def runnable[A](f: => A) = new Runnable {
    override def run(): Unit = f
  }
}
