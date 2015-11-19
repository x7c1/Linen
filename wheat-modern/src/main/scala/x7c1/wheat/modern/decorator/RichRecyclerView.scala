package x7c1.wheat.modern.decorator


import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.OnScrollListener


class RichRecyclerView[A <: RecyclerView](view: A){
  def onScroll[B](f: ScrollEvent[A] => B): Unit = {
    view addOnScrollListener new OnScroll(f)
  }
}

private class OnScroll[A <: RecyclerView, B](
  f: ScrollEvent[A] => B) extends OnScrollListener {

  override def onScrolled(view: RecyclerView, x: Int, y: Int): Unit = {
    val event = new ScrollEvent[A]{
      override def targetView: A = view.asInstanceOf[A]
      override def dx: Int = x
      override def dy: Int = y
    }
    f(event)
  }

}
