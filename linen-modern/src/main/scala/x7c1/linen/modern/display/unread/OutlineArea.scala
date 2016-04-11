package x7c1.linen.modern.display.unread

import android.support.v7.widget.{RecyclerView, Toolbar}
import x7c1.wheat.modern.decorator.Imports._
import x7c1.wheat.modern.tasks.ScrollerTasks

class OutlineArea(
  toolbar: Toolbar,
  protected val recyclerView: RecyclerView,
  getPosition: () => Int ) extends Pane {

  override lazy val displayPosition: Int = getPosition()

  override protected val scrollerTasks =
    ScrollerTasks(
      recyclerView,
      flowSpaceDip = 50,
      flowTimePerInch = 200F
    )

  def updateToolbar(title: String): Unit = {
    toolbar runUi { _ setTitle title }
  }
}
