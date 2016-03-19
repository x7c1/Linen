package x7c1.linen.modern.display.unread

import android.support.v7.widget.RecyclerView.Adapter
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.ViewGroup
import x7c1.linen.glue.res.layout.{UnreadDetailRow, UnreadDetailRowEntry, UnreadDetailRowFooter, UnreadDetailRowSource}
import x7c1.linen.modern.accessor.unread.{EntryContent, SourceHeadlineContent}
import x7c1.linen.modern.accessor.{EntryAccessor, SourceKind}
import x7c1.linen.modern.struct.{UnreadDetail, UnreadEntry}
import x7c1.wheat.ancient.resource.ViewHolderProvider
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.Imports._


class DetailRowAdapter(
  entryAccessor: EntryAccessor[UnreadDetail],
  selectedListener: OnDetailSelectedListener,
  visitSelectedListener: OnEntryVisitListener[UnreadDetail],
  sourceProvider: ViewHolderProvider[UnreadDetailRowSource],
  entryProvider: ViewHolderProvider[UnreadDetailRowEntry],
  footerProvider: ViewHolderProvider[UnreadDetailRowFooter],
  footerHeight: => Int ) extends Adapter[UnreadDetailRow] {

  override def getItemCount: Int = {
    // +1 to include footer
    entryAccessor.length + 1
  }

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
  override def onBindViewHolder(holder: UnreadDetailRow, position: Int): Unit = {
    entryAccessor.bindViewHolder(holder, position){
      case (row: UnreadDetailRowEntry, EntryContent(entry)) =>
        row.title.text = entry.fullTitle
        row.content.text = Html.fromHtml(entry.fullContent)
        row.content setMovementMethod LinkMovementMethod.getInstance()
        row.createdAt.text = entry.createdAt.format
        row.itemView onClick { _ =>
          val event = DetailSelectedEvent(position, entry)
          selectedListener onEntryDetailSelected event
        }
        row.visit onClick { _ =>
          visitSelectedListener onVisit entry
        }
      case (row: UnreadDetailRowSource, source: SourceHeadlineContent) =>
        row.title.text = source.title
      case (row: UnreadDetailRowFooter, _) =>
        Log info s"footer"
    }
  }
  override def getItemViewType(position: Int): Int = {
    val provider = position match {
      case x if x == entryAccessor.length => footerProvider
      case _ => entryAccessor findKindAt position match {
        case Some(SourceKind) => sourceProvider
        case _ => entryProvider
      }
    }
    provider.layoutId
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
