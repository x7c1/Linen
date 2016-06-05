package x7c1.linen.modern.display.unread

import x7c1.linen.glue.res.layout.{UnreadOutlineRow, UnreadOutlineRowEntry, UnreadOutlineRowFooter, UnreadOutlineRowSource}
import x7c1.linen.repository.entry.unread.{EntryContent, EntryRowContent, SourceHeadlineContent, UnreadOutline}
import x7c1.wheat.lore.resource.AdapterDelegatee
import x7c1.wheat.lore.resource.AdapterDelegatee.BaseAdapter
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.Imports._

class OutlineRowAdapter(
  delegatee: AdapterDelegatee[UnreadOutlineRow, EntryRowContent[UnreadOutline]],
  entrySelectedListener: OnOutlineSelectedListener,
  footerHeight: => Int) extends BaseAdapter(delegatee) {

  override def onBindViewHolder(holder: UnreadOutlineRow, position: Int) = {
    delegatee.bindViewHolder(holder, position){
      case (row: UnreadOutlineRowEntry, EntryContent(entry)) =>
        row.title.text = entry.shortTitle
        row.itemView onClick { _ =>
          val event = OutlineSelectedEvent(position, entry)
          entrySelectedListener onEntrySelected event
        }
      case (row: UnreadOutlineRowSource, source: SourceHeadlineContent) =>
        row.title.text = source.title
      case (row: UnreadOutlineRowFooter, _) =>
        row.itemView updateLayoutParams { _.height = footerHeight }
        Log info s"footer"
    }
  }
}

trait OnOutlineSelectedListener {
  self =>
  def onEntrySelected(event: OutlineSelectedEvent): Unit
  def append(listener: OnOutlineSelectedListener): OnOutlineSelectedListener =
    new OnOutlineSelectedListener {
      override def onEntrySelected(event: OutlineSelectedEvent): Unit = {
        self onEntrySelected event
        listener onEntrySelected event
      }
    }
}

case class OutlineSelectedEvent(position: Int, entry: UnreadOutline){
  def dump: String = s"entryId:${entry.entryId}, position:$position"
}
