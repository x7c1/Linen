package x7c1.linen.modern.display

import android.support.v7.widget.Toolbar
import x7c1.wheat.modern.callback.OnFinish
import x7c1.wheat.modern.decorator.Imports._
import x7c1.wheat.modern.tasks.ScrollerTasks

class EntryArea(
  toolbar: Toolbar,
  scroller: ScrollerTasks,
  getPosition: () => Int ) extends Pane {

  override lazy val displayPosition: Int = getPosition()

  def updateToolbar(title: String): Unit = {
    toolbar runUi { _ setTitle title }
  }
  def scrollTo(position: Int)(done: OnFinish): Unit = {
    scroller.scrollTo(position)(done).execute()
  }
  def fastScrollTo(position: Int)(done: OnFinish): Unit = {
    scroller.fastScrollTo(position)(done).execute()
  }
}
