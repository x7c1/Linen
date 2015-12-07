package x7c1.linen.modern.display

import android.support.v7.widget.RecyclerView
import x7c1.linen.modern.accessor.SourceAccessor
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.callback.{CallbackTask, OnFinish}
import x7c1.wheat.modern.tasks.ScrollerTasks


class SourceArea(
  sources: SourceAccessor,
  recyclerView: RecyclerView,
  getPosition: () => Int ) extends Pane {

  override lazy val displayPosition: Int = getPosition()

  private val scroller = ScrollerTasks(recyclerView)

  def skipTo(position: Int): CallbackTask[Unit] = {
    scroller skipTo position
  }
  def scrollTo(position: Int)(done: OnFinish): Unit = {
    Log info s"[init] position:$position"
    scroller.scrollTo(position)(done).execute()
  }
  def fastScrollTo(position: Int)(done: OnFinish): Unit = {
    Log info s"[init] position:$position"
    scroller.fastScrollTo(position)(done).execute()
  }
}
