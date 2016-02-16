package x7c1.linen.modern.display.unread

import android.support.v7.widget.RecyclerView.Adapter
import android.view.ViewGroup
import x7c1.linen.glue.res.layout.{UnreadOutlineRow, UnreadOutlineRowEntry, UnreadOutlineRowSource}
import x7c1.linen.modern.accessor.{EntryAccessor, EntryRow, SourceKind}
import x7c1.linen.modern.struct.UnreadOutline
import x7c1.wheat.ancient.resource.ViewHolderProvider
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.Imports._

class OutlineRowAdapter(
  entryAccessor: EntryAccessor[UnreadOutline],
  entrySelectedListener: OnOutlineSelectedListener,
  sourceProvider: ViewHolderProvider[UnreadOutlineRowSource],
  entryProvider: ViewHolderProvider[UnreadOutlineRowEntry] ) extends Adapter[UnreadOutlineRow] {

  override def getItemCount = {
    entryAccessor.length
  }
  override def onCreateViewHolder(parent: ViewGroup, viewType: Int) = {
    val provider = viewType match {
      case x if x == sourceProvider.layoutId => sourceProvider
      case _ => entryProvider
    }
    provider inflateOn parent
  }
  override def onBindViewHolder(holder: UnreadOutlineRow, position: Int) = {
    entryAccessor.findAt(position) -> holder match {
      case (Some(EntryRow(Right(entry))), holder: UnreadOutlineRowEntry) =>
        holder.title.text = entry.shortTitle
        holder.itemView onClick { _ =>
          val event = OutlineSelectedEvent(position, entry)
          entrySelectedListener onEntrySelected event
        }

      case (Some(EntryRow(Left(source))), holder: UnreadOutlineRowSource) =>
        holder.title.text = source.title
        Log info s"source $source"

      case (outline, row) =>
        Log error s"unknown outline:$outline, holder:$row"
    }
  }
  override def getItemViewType(position: Int): Int = {
    val provider = entryAccessor findKindAt position match {
      case Some(SourceKind) => sourceProvider
      case _ => entryProvider
    }
    provider.layoutId
  }
}

trait OnOutlineSelectedListener {
  def onEntrySelected(event: OutlineSelectedEvent): Unit
}

case class OutlineSelectedEvent(position: Int, entry: UnreadOutline){
  def dump: String = s"entryId:${entry.entryId}, position:$position"
}
