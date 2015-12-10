package x7c1.linen.modern.display

import android.support.v7.widget.{RecyclerView, Toolbar}
import x7c1.linen.modern.accessor.{EntryAccessor, SourceAccessor}
import x7c1.wheat.modern.callback.{CallbackTask, OnFinish}
import x7c1.wheat.modern.decorator.Imports._
import x7c1.wheat.modern.tasks.ScrollerTasks

class EntryDetailArea(
  sources: SourceAccessor,
  entries: EntryAccessor,
  toolbar: Toolbar,
  recyclerView: RecyclerView,
  getPosition: () => Int ) extends Pane {

  override lazy val displayPosition: Int = getPosition()

  private val scroller = ScrollerTasks(recyclerView)

  def updateToolbar(entryPosition: Int): Unit = {
    val entry = entries get entryPosition
    toolbar runUi {_ setTitle entry.title}
  }
  def fastScrollTo(position: Int)(done: OnFinish): CallbackTask[Unit] = {
    scroller.fastScrollTo(position)(done)
  }
  def scrollTo(position: Int)(done: OnFinish): CallbackTask[Unit] = {
    scroller.scrollTo(position)(done)
  }
  def skipTo(position: Int): CallbackTask[Unit] = {
    scroller skipTo position
  }
}
