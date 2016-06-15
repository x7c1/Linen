package x7c1.linen.modern.init.settings.order

import android.support.v4.view.MotionEventCompat
import android.support.v7.widget.RecyclerView.ViewHolder
import android.view.{MotionEvent, View}
import android.view.View.OnTouchListener
import x7c1.linen.glue.res.layout.{SettingChannelOrderRow, SettingChannelOrderRowItem}
import x7c1.linen.repository.channel.subscribe.SubscribedChannel
import x7c1.wheat.lore.resource.AdapterDelegatee
import x7c1.wheat.lore.resource.AdapterDelegatee.BaseAdapter
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.Imports._

class ChannelOrderRowAdapter(
  delegatee: AdapterDelegatee[SettingChannelOrderRow, SubscribedChannel],
  onDragStart: ViewHolder => Unit
) extends BaseAdapter(delegatee){
  override def onBindViewHolder(holder: SettingChannelOrderRow, i: Int): Unit = {
    delegatee.bindViewHolder(holder, i){
      case (row: SettingChannelOrderRowItem, channel) =>
        row.name.text = channel.name

        val listener = createOnTouchListener(row)
        row.startDraggingLeft setOnTouchListener listener
        row.startDraggingRight setOnTouchListener listener

      case (x, y) =>
        Log info s"unknown row: $x $y"
    }

  }
  private def createOnTouchListener(row: SettingChannelOrderRow) = new OnTouchListener {
    override def onTouch(v: View, event: MotionEvent): Boolean = {
      if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN){
        onDragStart(row)
      }
      false
    }
  }
}
