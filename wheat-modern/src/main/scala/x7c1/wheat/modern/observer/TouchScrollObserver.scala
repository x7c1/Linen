package x7c1.wheat.modern.observer

import android.support.v7.widget.RecyclerView
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.chrono.BufferingChangeWatcher


trait TouchScrollObserver {
  def touch(): Unit
}

class VerticalTouchScrollObserver(
  recyclerView: RecyclerView,
  listener: OnScrollStoppedListener ) extends TouchScrollObserver {

  private val watcher = new BufferingChangeWatcher[Int](
    delay = 50,
    period = 75,
    getCurrent = () => {
      recyclerView.computeVerticalScrollOffset()
    },
    onStop = current => {
      listener onScrollStopped new ScrollStoppedEvent(current)
    },
    onError = e => {
      Log error e.toString
    }
  )
  override def touch(): Unit = watcher.touch()
}
