package x7c1.linen.modern.display

import android.support.v7.widget.RecyclerView
import x7c1.linen.modern.accessor.UnreadSourceAccessor
import x7c1.wheat.modern.tasks.ScrollerTasks


class SourceArea(
  sources: UnreadSourceAccessor,
  recyclerView: RecyclerView,
  getPosition: () => Int ) extends Pane {

  override lazy val displayPosition: Int = getPosition()

  override protected val scrollerTasks =
    ScrollerTasks(
      recyclerView,
      flowSpaceDip = 30,
      flowTimePerInch = 300F
    )
}
