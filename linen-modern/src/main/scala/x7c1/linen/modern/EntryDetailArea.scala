package x7c1.linen.modern

import android.support.v7.widget.{RecyclerView, Toolbar}
import x7c1.wheat.modern.callback.{CallbackTask, OnFinish}
import x7c1.wheat.modern.decorator.Imports._
import x7c1.wheat.modern.tasks.ScrollerTasks

class EntryDetailArea(
  entries: EntryAccessor,
  toolbar: Toolbar,
  recyclerView: RecyclerView,
  getPosition: () => Int ) extends Pane {

  override lazy val displayPosition: Int = getPosition()

  private val tasks = ScrollerTasks(recyclerView, 30F)

  def updateToolbar(entryId: Long): Unit = {
    val position = entries indexOf entryId
    val entry = entries.get(position)
    toolbar runUi {_ setTitle entry.title}
  }
  def fastScrollTo(position: Int)(done: OnFinish): CallbackTask[Unit] = {
    tasks.fastScrollTo(position)(done)
  }
}
