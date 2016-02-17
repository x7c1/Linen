package x7c1.linen.modern.display.unread

import android.support.v7.widget.RecyclerView.Adapter
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.ViewGroup
import x7c1.linen.glue.res.layout.UnreadDetailRowEntry
import x7c1.linen.modern.accessor.{EntryRow, EntryAccessor}
import x7c1.linen.modern.struct.{UnreadEntry, UnreadDetail}
import x7c1.wheat.ancient.resource.ViewHolderProvider
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.Imports._


class DetailRowAdapter(
  entryAccessor: EntryAccessor[UnreadDetail],
  selectedListener: OnDetailSelectedListener,
  visitSelectedListener: OnEntryVisitListener[UnreadDetail],
  viewHolderProvider: ViewHolderProvider[UnreadDetailRowEntry]) extends Adapter[UnreadDetailRowEntry] {

  override def getItemCount: Int = entryAccessor.length

  override def onCreateViewHolder(viewGroup: ViewGroup, i: Int): UnreadDetailRowEntry = {
    viewHolderProvider.inflateOn(viewGroup)
  }
  override def onBindViewHolder(holder: UnreadDetailRowEntry, position: Int): Unit = {
    entryAccessor findAt position foreach {
      case EntryRow(Right(entry)) =>
        holder.title.text = entry.fullTitle
        holder.content.text = Html.fromHtml(entry.fullContent)
        holder.content setMovementMethod LinkMovementMethod.getInstance()
        holder.createdAt.text = entry.createdAt.format
        holder.itemView onClick { _ =>
          val event = DetailSelectedEvent(position, entry)
          selectedListener onEntryDetailSelected event
        }
        holder.visit onClick { _ =>
          visitSelectedListener onVisit entry
        }
      case EntryRow(Left(source)) =>
        holder.title.text = source.title
        holder.content.text = ""
        holder.createdAt.text = ""

        Log info s"source $source"
    }
  }

}

trait OnDetailSelectedListener {
  def onEntryDetailSelected(event: DetailSelectedEvent): Unit
}

case class DetailSelectedEvent(position: Int, entry: UnreadDetail){
  def dump: String = s"position:$position, entry:$entry"
}

trait OnEntryVisitListener[A <: UnreadEntry]{
  def onVisit(target: A): Unit
}
