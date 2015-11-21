package x7c1.wheat.modern.decorator

import android.content.Context
import android.view.View
import android.view.View.OnClickListener
import x7c1.wheat.modern.kinds.CallbackTask

class RichView[A <: View](view: A){

  def context: Context = view.getContext

  def onClick[B](f: A => B): Unit = view.setOnClickListener(new OnClickListener {
    override def onClick(view: View): Unit = f(view.asInstanceOf[A])
  })

  def runUi[B](f: A => B): Unit = {
    view post new Runnable {
      override def run(): Unit = f(view)
    }
  }
}

object UiThreadTask {
  def from[A <: View](view: A): UiThreadTask[A] = new UiThreadTask(view)
}

class UiThreadTask[A <: View](view: A){
  import Imports.toRichView

  def apply[B](procedure: A => B): CallbackTask[MainThreadDummyEvent[B]] = {
    val f: (MainThreadDummyEvent[B] => Unit) => Unit = { onFinish =>
      view runUi { _ =>
        val value = procedure(view)
        onFinish(new MainThreadDummyEvent(value))
      }
    }
    CallbackTask(f)
  }
}

class MainThreadDummyEvent[A](val value: A)
