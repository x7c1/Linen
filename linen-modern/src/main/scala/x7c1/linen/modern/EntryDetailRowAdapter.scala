package x7c1.linen.modern

import android.support.v7.widget.RecyclerView.Adapter
import android.view.ViewGroup
import x7c1.linen.glue.res.layout.EntryDetailRow
import x7c1.wheat.ancient.resource.ViewHolderProvider
import x7c1.wheat.modern.decorator.Imports._


class EntryDetailRowAdapter(
  entryAccessor: EntryAccessor,
  viewHolderProvider: ViewHolderProvider[EntryDetailRow]) extends Adapter[EntryDetailRow] {

  override def getItemCount: Int = entryAccessor.length

  override def onCreateViewHolder(viewGroup: ViewGroup, i: Int): EntryDetailRow = {
    viewHolderProvider.inflateOn(viewGroup)
  }

  override def onBindViewHolder(holder: EntryDetailRow, position: Int): Unit = {
    val entry = entryAccessor get position
    holder.title.text = entry.title
    holder.content.text = entry.content
    holder.createdAt.text = entry.createdAt.format
  }
}
