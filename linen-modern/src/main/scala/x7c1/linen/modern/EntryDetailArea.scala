package x7c1.linen.modern

import android.support.v7.widget.RecyclerView
import x7c1.wheat.modern.callback.CallbackTask.task
import x7c1.wheat.modern.callback.{CallbackTask, OnFinish}
import x7c1.wheat.modern.tasks.ScrollerTasks
import x7c1.wheat.modern.callback.Imports._

class EntryDetailArea(
  entries: EntryAccessor,
  recyclerView: RecyclerView,
  getPosition: () => Int ) extends Pane {

  override lazy val displayPosition: Int = getPosition()

  private val tasks = ScrollerTasks(recyclerView, 45F)

  def displayFirstEntryOf(sourceId: Long)(done: OnFinish): CallbackTask[Unit] = {
    for {
      Some(entryId)<- task(entries.firstEntryIdOf(sourceId))
      _ <- task of tasks.fastScrollTo(entries indexOf entryId) _
    } yield ()
  }

  def fastScrollTo(position: Int)(done: OnFinish): CallbackTask[Unit] = {
    tasks.fastScrollTo(position)(done)
  }
}
