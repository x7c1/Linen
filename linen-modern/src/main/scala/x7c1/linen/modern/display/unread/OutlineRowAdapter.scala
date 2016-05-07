package x7c1.linen.modern.display.unread

import android.support.v7.widget.RecyclerView.Adapter
import android.view.ViewGroup
import x7c1.linen.glue.res.layout.{UnreadOutlineRow, UnreadOutlineRowEntry, UnreadOutlineRowFooter, UnreadOutlineRowSource}
import x7c1.linen.modern.init.unread.OutlineListProviders
import x7c1.linen.repository.entry.unread.{EntryAccessor, EntryContent, SourceHeadlineContent, UnreadOutline}
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.Imports._

class OutlineRowAdapter(
  entryAccessor: EntryAccessor[UnreadOutline],
  entrySelectedListener: OnOutlineSelectedListener,
  providers: OutlineListProviders,
  footerHeight: => Int) extends Adapter[UnreadOutlineRow] {

  override def getItemCount = entryAccessor.length

  override def onCreateViewHolder(parent: ViewGroup, viewType: Int) = {
    providers.createViewHolder(parent, viewType)
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
        row.itemView updateLayoutParams { _.height = footerHeight }
        Log info s"footer"
    }
  }
  override def getItemViewType(position: Int): Int = viewTypeAt(position)

  private lazy val viewTypeAt = providers createViewTyper entryAccessor
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
