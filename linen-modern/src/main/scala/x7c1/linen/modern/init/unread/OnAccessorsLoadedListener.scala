package x7c1.linen.modern.init.unread

import x7c1.linen.glue.res.layout.UnreadItemsLayout
import x7c1.linen.modern.accessor.unread.{ChannelSelectable, UnreadSourceAccessor}
import x7c1.linen.modern.action.observer.SourceSkipStoppedObserver
import x7c1.linen.modern.action.{Actions, DrawerAction, SourceSkipStoppedFactory}
import x7c1.linen.modern.display.unread.PaneContainer
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.callback.CallbackTask.task
import x7c1.wheat.modern.tasks.UiThread


class OnAccessorsLoadedListener(
  layout: UnreadItemsLayout,
  container: PaneContainer,
  pointer: SourcePointer,
  drawer: => DrawerAction ){

  def onLoad[A: ChannelSelectable](e: LoadCompleteEvent[A]): Unit = {
    Log info s"[init] $e"

    val tasks = for {
      ui <- task { UiThread via layout.itemView }
      _ <- ui { _ =>
        layout.sourceToolbar setTitle e.channelName
        layout.entryToolbar setTitle ""
        layout.entryDetailToolbar setTitle ""
        updateAdapter()
        pointer focusOn 0
      }
      _ <- container.sourceArea skipTo 0
      _ <- container skipTo container.sourceArea
      _ <- ui join container.fadeIn()
      _ <- ui join task { drawer.onBack() }
    } yield ()

    tasks.execute()
  }
  private def updateAdapter() = {

    /*
    2015-12-20:
    it should be written like:
      layout.sourceList.getAdapter.notifyItemRangeInserted(0, ...)

    but this 'notifyItemRangeInserted' causes following error (and crash)
      java.lang.IndexOutOfBoundsException:
        Inconsistency detected. Invalid view holder adapter positionViewHolder
    */

    layout.sourceList.getAdapter.notifyDataSetChanged()
    layout.entryList.getAdapter.notifyDataSetChanged()
    layout.entryDetailList.getAdapter.notifyDataSetChanged()
  }
}

class SourcePointer(
  accessor: UnreadSourceAccessor,
  container: PaneContainer,
  actions: Actions ){

  private val factory = new SourceSkipStoppedFactory(accessor)

  private val observer = new SourceSkipStoppedObserver(actions)

  def focusOn(position: Int): Unit = {
    factory.createAt(position) match {
      case Some(event) => observer onSkipStopped event
      case None => Log error s"no source row at($position)"
    }
  }
}
