package x7c1.linen.modern.display.unread

import x7c1.linen.glue.res.layout.{UnreadDetailRow, UnreadDetailRowEntry, UnreadDetailRowFooter, UnreadDetailRowSource}
import x7c1.linen.repository.entry.unread.{EntryContent, EntryRowContent, SourceHeadlineContent, UnreadDetail, UnreadEntry}
import x7c1.wheat.lore.resource.AdapterDelegatee
import x7c1.wheat.lore.resource.AdapterDelegatee.BaseAdapter
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.action.SiteVisitable
import x7c1.wheat.modern.decorator.Imports._


class DetailRowAdapter(
  delegatee: AdapterDelegatee[UnreadDetailRow, EntryRowContent[UnreadDetail]],
  selectedListener: OnDetailSelectedListener,
  visitSelectedListener: OnEntryVisitListener[UnreadDetail],
  laterSelectedListener: OnLaterSelectedListener,
  footerHeight: => Int ) extends BaseAdapter(delegatee) {

  override def onBindViewHolder(holder: UnreadDetailRow, position: Int): Unit = {
    delegatee.bindViewHolder(holder, position){
      case (row: UnreadDetailRowEntry, EntryContent(entry)) =>
        row.title.text = entry.fullTitle
        row.content setHtmlWithoutImage entry.fullContent

        row.itemView onClick { _ =>
          val event = DetailSelectedEvent(position, entry)
          selectedListener onEntryDetailSelected event
        }
        row.visit onClick { _ =>
          visitSelectedListener onVisit entry
        }
        row.later onClick { _ =>
          val event = LaterSelectedEvent(entry)
          laterSelectedListener onLaterSelected event
        }
      case (row: UnreadDetailRowSource, source: SourceHeadlineContent) =>
        row.title.text = source.title

      case (row: UnreadDetailRowFooter, _) =>
        row.itemView updateLayoutParams { _.height = footerHeight }
        Log info s"footer"
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

trait OnLaterSelectedListener {
  def onLaterSelected[A: SiteVisitable](event: LaterSelectedEvent[A])
}

case class LaterSelectedEvent[A: SiteVisitable](target: A)
