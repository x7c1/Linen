package x7c1.linen.modern.display

import android.support.v7.widget.RecyclerView.Adapter
import android.view.ViewGroup
import x7c1.linen.glue.res.layout.EntryDetailRow
import x7c1.linen.modern.accessor.EntryAccessor
import x7c1.linen.modern.struct.EntryDetail
import x7c1.wheat.ancient.resource.ViewHolderProvider
import x7c1.wheat.modern.decorator.Imports._


class EntryDetailRowAdapter(
  entryAccessor: EntryAccessor[EntryDetail],
  selectedListener: OnEntryDetailSelectedListener,
  viewHolderProvider: ViewHolderProvider[EntryDetailRow]) extends Adapter[EntryDetailRow] {

  override def getItemCount: Int = entryAccessor.length

  override def onCreateViewHolder(viewGroup: ViewGroup, i: Int): EntryDetailRow = {
    viewHolderProvider.inflateOn(viewGroup)
  }

  override def onBindViewHolder(holder: EntryDetailRow, position: Int): Unit = {
    entryAccessor findAt position foreach { entry =>
      holder.title.text = entry.fullTitle
      holder.content.text = entry.fullContent
      holder.createdAt.text = entry.createdAt.format
      holder.itemView onClick { _ =>
        val event = EntryDetailSelectedEvent(position, entry)
        selectedListener onEntryDetailSelected event
      }
    }
  }

}

trait OnEntryDetailSelectedListener {
  def onEntryDetailSelected(event: EntryDetailSelectedEvent)
}

case class EntryDetailSelectedEvent(position: Int, entry: EntryDetail){
  def dump: String = s"position:$position, entry:$entry"
}
