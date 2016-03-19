package x7c1.linen.modern.display.unread

import android.support.v7.widget.RecyclerView.Adapter
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.ViewGroup
import x7c1.linen.glue.res.layout.{UnreadDetailRow, UnreadDetailRowEntry, UnreadDetailRowFooter, UnreadDetailRowSource}
import x7c1.linen.modern.accessor.unread.{EntryAccessor, EntryContent, SourceHeadlineContent}
import x7c1.linen.modern.init.unread.DetailListProviders
import x7c1.linen.modern.struct.{UnreadDetail, UnreadEntry}
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.Imports._


class DetailRowAdapter(
  entryAccessor: EntryAccessor[UnreadDetail],
  selectedListener: OnDetailSelectedListener,
  visitSelectedListener: OnEntryVisitListener[UnreadDetail],
  providers: DetailListProviders,
  footerHeight: => Int ) extends Adapter[UnreadDetailRow] {

  override def getItemCount = entryAccessor.length

  override def onCreateViewHolder(parent: ViewGroup, viewType: Int) = {
    providers.getWithLayoutParams(parent, viewType){
      case (_: UnreadDetailRowFooter, params) =>
        params.height = footerHeight
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
  override def getItemViewType(position: Int) = viewTypeAt(position)

  private lazy val viewTypeAt = providers createViewTyper entryAccessor
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
