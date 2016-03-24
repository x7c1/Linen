package x7c1.linen.modern.init.unread

import x7c1.linen.glue.res.layout.MainLayout
import x7c1.linen.modern.accessor.unread.ChannelSelectable
import x7c1.linen.modern.action.DrawerAction
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.callback.CallbackTask.task
import x7c1.wheat.modern.tasks.Async.await
import x7c1.wheat.modern.tasks.UiThread


class OnAccessorsLoadedListener(layout: MainLayout, drawer: => DrawerAction){
  def onLoad[A: ChannelSelectable](e: LoadCompleteEvent[A]): Unit = {
    Log info s"[init] $e"

    (for {
      ui <- task {
        UiThread via layout.itemView
      }
      _ <- ui { _ => updateAdapter() }
      _ <- await(50)
      _ <- ui { _ => drawer.onBack() }
    } yield ()).execute()
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
