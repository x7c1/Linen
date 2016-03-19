package x7c1.linen.modern.display.unread

import android.support.v7.widget.RecyclerView.Adapter
import android.view.ViewGroup
import x7c1.linen.glue.res.layout.{UnreadOutlineRow, UnreadOutlineRowEntry, UnreadOutlineRowFooter, UnreadOutlineRowSource}
import x7c1.linen.modern.accessor.unread.{FooterKind, EntryKind, EntryAccessor, SourceKind, EntryContent, SourceHeadlineContent}
import x7c1.linen.modern.struct.UnreadOutline
import x7c1.wheat.ancient.resource.ViewHolderProvider
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.Imports._

class OutlineRowAdapter(
  entryAccessor: EntryAccessor[UnreadOutline],
  entrySelectedListener: OnOutlineSelectedListener,
  sourceProvider: ViewHolderProvider[UnreadOutlineRowSource],
  entryProvider: ViewHolderProvider[UnreadOutlineRowEntry],
  footerProvider: ViewHolderProvider[UnreadOutlineRowFooter],
  footerHeight: => Int) extends Adapter[UnreadOutlineRow] {

  override def getItemCount = entryAccessor.length

  override def onCreateViewHolder(parent: ViewGroup, viewType: Int) = {
    viewType match {
      case x if x == sourceProvider.layoutId =>
        sourceProvider inflateOn parent
      case x if x == entryProvider.layoutId =>
        entryProvider inflateOn parent
      case _ =>
        val holder = footerProvider inflateOn parent
        holder.itemView setLayoutParams {
          val params = holder.itemView.getLayoutParams
          params.height = footerHeight
          params
        }
        holder
    }
  }
  override def onBindViewHolder(holder: UnreadOutlineRow, position: Int) = {
    entryAccessor.bindViewHolder(holder, position){
      case (row: UnreadOutlineRowEntry, EntryContent(entry)) =>
        row.title.text = entry.shortTitle
        row.itemView onClick { _ =>
          val event = OutlineSelectedEvent(position, entry)
          entrySelectedListener onEntrySelected event
        }
      case (row: UnreadOutlineRowSource, source: SourceHeadlineContent) =>
        row.title.text = source.title
      case (row: UnreadOutlineRowFooter, _) =>
        Log info s"footer"
    }
  }
  override def getItemViewType(position: Int): Int = {
    providerAt(position).layoutId
  }
  private lazy val providerAt = entryAccessor createPositionMap {
    case SourceKind => sourceProvider
    case EntryKind => entryProvider
    case FooterKind => footerProvider
  }
}

trait OnOutlineSelectedListener {
  def onEntrySelected(event: OutlineSelectedEvent): Unit
}

case class OutlineSelectedEvent(position: Int, entry: UnreadOutline){
  def dump: String = s"entryId:${entry.entryId}, position:$position"
}
