package x7c1.linen.modern

import android.view.ViewGroup
import android.widget.Scroller
import x7c1.wheat.macros.logger.Log

class PaneContainer(
  view: ViewGroup,
  val sourceArea: SourceArea,
  val entryArea: EntryArea ) {

  private lazy val scroller = new Scroller(view.getContext)

  def scrollTo(pane: Pane)(onFinish: ContainerFocusedEvent => Unit): Unit = {
    val current = view.getScrollX
    val dx = pane.displayPosition - current
    val duration = 350

    Log info s"[init] current:$current, dx:$dx"
    scroller.startScroll(current, 0, dx, 0, duration)

    view.post(new ContainerScroller(onFinish))
  }
  private class ContainerScroller(
    onFinish: ContainerFocusedEvent => Unit) extends Runnable {

    override def run(): Unit = {
      val more = scroller.computeScrollOffset()
      val current = scroller.getCurrX
      if (more){
        view.scrollTo(current, 0)
        view.post(this)
      } else {
        Log info s"[done] current:$current"
        onFinish(new ContainerFocusedEvent)
      }
    }
  }
}

class ContainerFocusedEvent
