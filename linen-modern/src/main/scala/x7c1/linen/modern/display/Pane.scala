package x7c1.linen.modern.display

import android.view.ViewGroup
import android.widget.Scroller
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.callback.OnFinish

trait Pane {
  def displayPosition: Int
}

class PaneContainer(view: ViewGroup) {

  private lazy val scroller = new Scroller(view.getContext)

  def scrollTo(pane: Pane): OnFinish => Unit = done => {
    val current = view.getScrollX
    val dx = pane.displayPosition - current
    val duration = 300

    Log info s"[init] current:$current, dx:$dx"
    scroller.startScroll(current, 0, dx, 0, duration)

    view.post(new ContainerScroller(done))
  }
  private class ContainerScroller(done: OnFinish) extends Runnable {

    override def run(): Unit = {
      val more = scroller.computeScrollOffset()
      val current = scroller.getCurrX
      if (more){
        view.scrollTo(current, 0)
        view.post(this)
      } else {
        Log info s"[done] current:$current"
        done.evaluate()
      }
    }
  }
}
