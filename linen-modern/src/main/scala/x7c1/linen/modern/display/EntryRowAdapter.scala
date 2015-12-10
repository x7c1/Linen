package x7c1.linen.modern.display

import android.animation.{AnimatorSet, ObjectAnimator}
import android.support.v7.widget.RecyclerView.Adapter
import android.view.{View, ViewGroup}
import x7c1.linen.glue.res.layout.EntryRow
import x7c1.linen.modern.accessor.EntryAccessor
import x7c1.linen.modern.struct.Entry
import x7c1.wheat.ancient.resource.ViewHolderProvider
import x7c1.wheat.modern.callback.CallbackTask.task
import x7c1.wheat.modern.tasks.UiThread.via
import x7c1.wheat.modern.decorator.Imports._
import x7c1.wheat.modern.tasks.Async.await


class EntryRowAdapter(
  entryAccessor: EntryAccessor,
  entrySelectedListener: OnEntrySelectedListener,
  provider: ViewHolderProvider[EntryRow]) extends Adapter[EntryRow] {

  override def getItemCount = {
    entryAccessor.length
  }
  override def onCreateViewHolder(parent: ViewGroup, viewType: Int) = {
    provider inflateOn parent
  }
  override def onBindViewHolder(holder: EntryRow, position: Int) = {
    val display = for {
      Some(entry) <- task { entryAccessor findAt position }
      _ <- task {
        holder.title.text = entry.title
        holder.content.text = ""
        holder.createdAt.text = ""
        holder.itemView onClick { _ =>
          val event = EntrySelectedEvent(position, entry)
          entrySelectedListener onEntrySelected event
        }
      }
      _ <- await(300)
      animator <- task(new AnimatorSet) if position == holder.getAdapterPosition
      _ <- task apply animator.playTogether(
        fadeIn(holder.content),
        fadeIn(holder.createdAt)
      )
      _ <- via(holder.itemView){ _ =>
        animator.start()
        holder.content.text = entry.content
        holder.createdAt.text = entry.createdAt.format
      }
    } yield ()

    display.execute()
  }

  private def fadeIn(view: View) = {
    ObjectAnimator.ofFloat(view, "alpha", 0.3F, 1f).setDuration(300)
  }

}

trait OnEntrySelectedListener {
  def onEntrySelected(event: EntrySelectedEvent): Unit
}

case class EntrySelectedEvent(position: Int, entry: Entry){
  def dump: String = s"entryId:${entry.entryId}, position:$position"
}
