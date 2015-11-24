package x7c1.linen.modern

import android.support.v7.widget.RecyclerView
import x7c1.wheat.modern.chrono.BufferingChangeWatcher

class VerticalScrollObserver(
  view: RecyclerView,
  listener: OnScrollStoppedListener ) {

  private val watcher = new BufferingChangeWatcher[Int](
    delay = 50,
    period = 75,
    getCurrent = () => view.computeVerticalScrollOffset(),
    onStop = current => {
      listener onScrollStopped new ScrollStoppedEvent(current)
    }
  )
  def touch(): Unit = watcher.touch()
}

trait OnScrollStoppedListener {
  def onScrollStopped(e: ScrollStoppedEvent)
}

class ScrollStoppedEvent(offset: Int)
