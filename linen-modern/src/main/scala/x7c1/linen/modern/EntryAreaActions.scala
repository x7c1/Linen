package x7c1.linen.modern

import android.support.v7.widget.{LinearLayoutManager, RecyclerView}
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.UiThreadTask
import x7c1.wheat.modern.kinds.CallbackTask.task
import x7c1.wheat.modern.kinds.OnFinish


class EntryAreaActions(entriesView: RecyclerView) {

  def afterInserting(position: Int, length: Int)(done: OnFinish) = for {
    ui <- task {
      Log debug s"[init] entries($length)"
      UiThreadTask from entriesView
    }
    _ <- ui { view =>
      val current = layoutManager.findFirstCompletelyVisibleItemPosition()
      val base = if(current == position) -1 else 0
      view.getAdapter.notifyItemRangeInserted(position + base, length)
    }
    _ <- scrollTo(position)(done)
  } yield ()

  def scrollTo(position: Int)(done: OnFinish) = for {
    ui <- task {
      Log info s"[init] position:$position"
      UiThreadTask from entriesView
    }
    _ <- ui { _ =>
      val current = layoutManager.findFirstCompletelyVisibleItemPosition()
      val diff = current - position
      val space = if (diff < 0) -1 else if(diff > 0) 1 else 0
      layoutManager.scrollToPositionWithOffset(position + space, 0)
    }
    _ <- ui { view =>
      val scroller = new SmoothScroller(
        view.getContext, timePerInch = 125F, layoutManager,
        done.by[ScrollerStopEvent]
      )
      scroller setTargetPosition position
      layoutManager startSmoothScroll scroller
    }
  } yield ()

  private lazy val layoutManager = {
    entriesView.getLayoutManager.asInstanceOf[LinearLayoutManager]
  }
}
