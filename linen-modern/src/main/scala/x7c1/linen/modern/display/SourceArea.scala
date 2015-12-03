package x7c1.linen.modern.display

import android.support.v7.widget.RecyclerView
import x7c1.linen.modern.accessor.SourceAccessor
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.callback.{CallbackTask, OnFinish}
import x7c1.wheat.modern.callback.CallbackTask.task
import x7c1.wheat.modern.tasks.ScrollerTasks


class SourceArea(
  sources: SourceAccessor,
  recyclerView: RecyclerView,
  getPosition: () => Int ) extends Pane {

  override lazy val displayPosition: Int = getPosition()

  private val tasks = ScrollerTasks(recyclerView, 125F)

  def display(sourceId: Long)(done: OnFinish): CallbackTask[Unit] =
    for {
      Some(position) <- task(sources positionOf sourceId)
      _ <- tasks.fastScrollTo(position)(done)
    } yield ()

  def scrollTo(position: Int)(done: OnFinish): Unit = {
    Log info s"[init] position:$position"
    tasks.scrollTo(position)(done).execute()
  }
  def fastScrollTo(position: Int)(done: OnFinish): Unit = {
    Log info s"[init] position:$position"
    tasks.fastScrollTo(position)(done).execute()
  }
}
