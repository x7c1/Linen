package x7c1.linen.modern

import android.support.v7.widget.RecyclerView.Adapter
import android.view.ViewGroup
import x7c1.linen.glue.res.layout.EntryDetailRow
import x7c1.wheat.ancient.resource.ViewHolderProvider
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.callback.CallbackTask
import x7c1.wheat.modern.decorator.Imports._

import scalaz.{-\/, \/-}
import scalaz.concurrent.Task


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
      val event = EntryDetailSelectedEvent(position, entry.entryId)
      selectedListener onEntryDetailSelected event
    }
  }
}

trait OnEntryDetailSelectedListener {
  def onEntryDetailSelected(event: EntryDetailSelectedEvent)
}

case class EntryDetailSelectedEvent(
  position: Int,
  entryId: Long
)

class EntryDetailSelectedObserver (
  container: PaneContainer ) extends OnEntryDetailSelectedListener {

  import x7c1.wheat.modern.callback.Imports._

  override def onEntryDetailSelected(event: EntryDetailSelectedEvent): Unit = {
    val focus = for {
      _ <- CallbackTask.task of container.entryDetailArea.scrollTo(event.position) _
    } yield {
      Log debug s"[ok] focus event:$event"
    }
    Seq(focus) foreach runAsync
  }
  def runAsync[A](task: CallbackTask[A]) = {
    Task(task.execute()) runAsync {
      case \/-(_) =>
      case -\/(e) => Log error e.toString
    }
  }

}
