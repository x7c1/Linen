package x7c1.linen.modern

import android.support.v7.widget.RecyclerView.Adapter
import android.view.ViewGroup
import x7c1.linen.glue.res.layout.EntryRow
import x7c1.wheat.ancient.resource.ViewHolderProvider
import x7c1.wheat.modern.decorator.Imports._


class EntryRowAdapter(
  entryAccessor: EntryAccessor,
  provider: ViewHolderProvider[EntryRow]) extends Adapter[EntryRow] {

  override def getItemCount = {
    entryAccessor.get.length
  }
  override def onCreateViewHolder(parent: ViewGroup, viewType: Int) = {
    provider inflateOn parent
  }
  override def onBindViewHolder(holder: EntryRow, position: Int) = {
    val entry = entryAccessor get position
    holder.title.text = entry.title
    holder.content.text = entry.content
    holder.createdAt.text = entry.createdAt.format
  }
}
