package x7c1.linen.modern

import android.support.v7.widget.RecyclerView.Adapter
import android.view.ViewGroup
import x7c1.linen.glue.res.layout.EntryDetailRow
import x7c1.wheat.ancient.resource.ViewHolderProvider
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.Imports._


class EntryDetailRowAdapter(
  entryAccessor: EntryAccessor,
  selectedListener: OnEntryDetailSelectedListener,
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
    holder.itemView onClick { _ =>
      val event = DetailSelectedEvent(position, entry)
      selectedListener onEntryDetailSelected event
    }
  }
}

trait OnEntryDetailSelectedListener {
  def onEntryDetailSelected(event: DetailSelectedEvent)
}

case class DetailSelectedEvent(position: Int, entry: Entry){
  def dump: String = s"position:$position, entry:$entry"
}

class EntryDetailSelectedObserver (actions: Actions)
  extends OnEntryDetailSelectedListener {

  import x7c1.linen.modern.CallbackTaskRunner.runAsync

  override def onEntryDetailSelected(event: DetailSelectedEvent): Unit = {
    Log info s"[init] ${event.dump}"

    val sync = for {
      _ <- actions.detailArea.onDetailSelected(event)
    } yield ()

    Seq(sync) foreach runAsync { Log error _.toString }
  }
}
