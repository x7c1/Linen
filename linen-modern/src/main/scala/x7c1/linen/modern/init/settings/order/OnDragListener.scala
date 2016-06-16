package x7c1.linen.modern.init.settings.order

import android.content.Context
import x7c1.linen.database.struct.HasAccountId
import x7c1.linen.glue.res.layout.SettingChannelOrderRowItem
import x7c1.linen.repository.channel.subscribe.SubscribedChannel
import x7c1.wheat.macros.intent.LocalBroadcaster
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.Imports.toRichTextView
import x7c1.wheat.modern.observer.recycler.order.DraggableSequenceRoute.{DragFinished, DragStarted, OnDragListener}


class OnDragListenerToStyle extends OnDragListener[SubscribedChannel]{
  override def onStartDragging(event: DragStarted[SubscribedChannel]) = {
    event.holder match {
      case row: SettingChannelOrderRowItem =>
        Log info s"start:${row.name.text} ${row.getAdapterPosition}"

        // todo: change color of row being dragged

      case _ =>
        Log error s"[failed] unknown type of ViewHolder: ${event.holder}"
    }
  }
  override def onFinishDragging(event: DragFinished[SubscribedChannel]) = {
    event.holder match {
      case row: SettingChannelOrderRowItem =>
        Log info s"[finish] ${row.getAdapterPosition}"

        // todo: revert color of dragged row

        event.sequence.toSeq foreach { channel =>
          Log info s"ch${channel.channelId}, ac:${channel.subscriberAccountId}, ${channel.channelRank}, ${channel.name}"
        }
      case _ =>
        Log error s"[failed] unknown type of ViewHolder: ${event.holder}"
    }
  }
}

class OnDragListenerToNotify[A: HasAccountId](context: Context) extends OnDragListener[A]{
  override def onStartDragging(event: DragStarted[A]): Unit = {
    // nop
  }
  override def onFinishDragging(event: DragFinished[A]): Unit = {
    event.sequence findAt 0 map implicitly[HasAccountId[A]].toId match {
      case Some(accountId) =>
        val event = ChannelOrdered(accountId)
        LocalBroadcaster(event) dispatchFrom context
      case None =>
        Log error s"empty sequence"
    }
  }
}
