package x7c1.linen.modern.display.unread

import android.support.v7.widget.RecyclerView.Adapter
import android.view.ViewGroup
import x7c1.linen.glue.res.layout.EntryRow
import x7c1.linen.modern.accessor.EntryAccessor
import x7c1.linen.modern.struct.EntryOutline
import x7c1.wheat.ancient.resource.ViewHolderProvider
import x7c1.wheat.modern.decorator.Imports._


class OutlineRowAdapter(
  entryAccessor: EntryAccessor[EntryOutline],
  entrySelectedListener: OnOutlineSelectedListener,
  provider: ViewHolderProvider[EntryRow]) extends Adapter[EntryRow] {

  override def getItemCount = {
    entryAccessor.length
  }
  override def onCreateViewHolder(parent: ViewGroup, viewType: Int) = {
    provider inflateOn parent
  }
  override def onBindViewHolder(holder: EntryRow, position: Int) = {
    entryAccessor findAt position foreach { entry =>
      holder.title.text = entry.shortTitle
      holder.itemView onClick { _ =>
        val event = OutlineSelectedEvent(position, entry)
        entrySelectedListener onEntrySelected event
      }
    }
  }
}

trait OnOutlineSelectedListener {
  def onEntrySelected(event: OutlineSelectedEvent): Unit
}

case class OutlineSelectedEvent(position: Int, entry: EntryOutline){
  def dump: String = s"entryId:${entry.entryId}, position:$position"
}
