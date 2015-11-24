package x7c1.wheat.modern.decorator

import android.content.Context
import android.view.View
import android.view.View.OnClickListener

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
