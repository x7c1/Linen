package x7c1.wheat.modern.decorator

import android.content.Context
import android.view.{ViewGroup, MotionEvent, View}
import android.view.View.{OnTouchListener, OnClickListener}

class RichView[A <: View](view: A){

  def context: Context = view.getContext

  def onClick[B](f: A => B): Unit = view.setOnClickListener(
    new OnClickListener {
      override def onClick(view: View): Unit = f(view.asInstanceOf[A])
    }
  )

  def onTouch(f: (A, MotionEvent) => Boolean): Unit = view.setOnTouchListener(
    new OnTouchListener {
      override def onTouch(v: View, event: MotionEvent): Boolean =
        f(v.asInstanceOf[A], event)
    }
  )
  def updateLayoutParams(f: ViewGroup.LayoutParams => Unit): Unit = {
    view setLayoutParams {
      val params = view.getLayoutParams
      f(params)
      params
    }
  }
  def runUi[B](f: A => B): Unit = {
    view post runnable(f)
  }

  def runAfter[B](msec: Long)(f: A => B): Unit = {
    view.postDelayed(runnable(f), msec)
  }

  private def runnable[B](f: A => B) = new Runnable {
    override def run(): Unit = f(view)
  }
}
