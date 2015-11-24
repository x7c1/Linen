package x7c1.linen.modern

import java.util.{Timer, TimerTask}

import android.support.v7.widget.RecyclerView

class VerticalScrollObserver(
  view: RecyclerView,
  onScrollStopped: OnScrollStoppedListener ) {

  private var position: Option[Int] = None

  private val timer = new Timer()

  private var task: Option[TimerTask] = None

  def touch(): Unit = {
    task.foreach{_.cancel()}
    task = Some apply newTask
    task foreach { timer.schedule(_, 100, 100) }
  }

  def cancel(): Unit = timer.cancel()

  private def newTask = new TimerTask {
    override def run(): Unit = {
      val current = view.computeVerticalScrollOffset()
      position match {
        case Some(previous) if previous == current =>
          cancel()
          onScrollStopped.onScrollStopped(new ScrollStoppedEvent(current))
        case _ =>
      }
      position = Some(current)
    }
  }
}

trait OnScrollStoppedListener {
  def onScrollStopped(e: ScrollStoppedEvent)
}

class ScrollStoppedEvent(offset: Int)