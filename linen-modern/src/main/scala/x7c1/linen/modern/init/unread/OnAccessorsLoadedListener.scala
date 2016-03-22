package x7c1.linen.modern.init.unread

import x7c1.linen.glue.res.layout.MainLayout
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.Imports._


class OnAccessorsLoadedListener(layout: MainLayout){
  def onLoad(e: AccessorsLoadedEvent): Unit = {
    layout.itemView runUi { _ =>

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

      Log info "[done]"
    }
  }
}
