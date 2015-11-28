package x7c1.linen.modern

import android.support.v7.widget.{LinearLayoutManager, RecyclerView}
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.callback.CallbackTask.task
import x7c1.wheat.modern.callback.{CallbackTask, UiThreadTask, OnFinish}


class RecyclerViewTasks(recyclerView: RecyclerView) {

  def notifyAndScroll(position: Int, length: Int)(done: OnFinish) = for {
    ui <- task {
      Log debug s"[init] position:$position, length:$length"
      UiThreadTask from recyclerView
    }
    _ <- ui { view =>
      val current = layoutManager.findFirstCompletelyVisibleItemPosition()
      val base = if(current == position) -1 else 0
      view.getAdapter.notifyItemRangeInserted(position + base, length)
    }
    _ <- fastScrollTo(position)(done)
  } yield ()

  def fastScrollTo(position: Int)(done: OnFinish): CallbackTask[Unit] = for {
    ui <- task {
      Log debug s"[init] position:$position"
      UiThreadTask from recyclerView
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

  def scrollTo(position: Int)(done: OnFinish) = for {
    ui <- task {
      UiThreadTask from recyclerView
    }
    scroller <- task {
      new SmoothScroller(
        recyclerView.getContext, timePerInch = 45F, layoutManager,
        done.by[ScrollerStopEvent] )
    }
    _ <- ui { _ =>
      scroller setTargetPosition position
      layoutManager startSmoothScroll scroller
    }
  } yield ()

  private lazy val layoutManager = {
    recyclerView.getLayoutManager.asInstanceOf[LinearLayoutManager]
  }
}
