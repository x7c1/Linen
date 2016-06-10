package x7c1.linen.modern.init.settings.order

import android.app.Activity
import android.support.v7.widget.RecyclerView.ViewHolder
import android.support.v7.widget.helper.ItemTouchHelper
import android.support.v7.widget.{LinearLayoutManager, RecyclerView}
import android.support.v7.widget.helper.ItemTouchHelper.{Callback, DOWN, UP}
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.database.struct.HasAccountId
import x7c1.linen.glue.activity.ActivityControl
import x7c1.linen.glue.res.layout.SettingChannelOrderLayout
import x7c1.linen.glue.service.ServiceControl
import x7c1.linen.repository.channel.subscribe.SubscribedChannel
import x7c1.wheat.lore.resource.AdapterDelegatee
import x7c1.wheat.macros.intent.IntentExpander
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.database.selector.presets.ClosableSequenceLoader
import x7c1.wheat.modern.database.selector.presets.ClosableSequenceLoader.{Done, SqlError}
import x7c1.wheat.modern.decorator.Imports.{toRichToolbar, toRichView}
import x7c1.wheat.modern.formatter.ThrowableFormatter.format

class ChannelOrderDelegatee (
  activity: Activity with ActivityControl with ServiceControl,
  layout: SettingChannelOrderLayout,
  providers: ChannelOrderRowProviders ){

  def onCreate(): Unit = {
    Log info s"[init]"
    layout.toolbar onClickNavigation { _ =>
      activity.finish()
    }
    val manager = new LinearLayoutManager(activity)
    layout.channelList setLayoutManager manager
    layout.channelList setAdapter new ChannelOrderRowAdapter(
      AdapterDelegatee.create(providers, loader.sequence)
    )
    val helper = new ItemTouchHelper(new DragControl)
    helper attachToRecyclerView layout.channelList
    layout.channelList addItemDecoration helper

    IntentExpander executeBy activity.getIntent
  }
  def onDestroy(): Unit = {
    Log info s"[init]"
    helper.close()
  }
  private lazy val helper = {
    new DatabaseHelper(activity)
  }
  private lazy val loader = {
    ClosableSequenceLoader[HasAccountId, SubscribedChannel](helper.getReadableDatabase)
  }
  def showChannels(accountId: Long): Unit = {
    Log info s"[init]"
    loader.startLoading(accountId) apply {
      case Done(_) =>
        Log info s"[start]"
        layout.channelList runUi {_.getAdapter.notifyDataSetChanged()}
      case SqlError(e) =>
        Log error format(e.getCause){"[failed]"}
    }
  }
}

class DragControl extends Callback {
  override def getMovementFlags(recyclerView: RecyclerView, viewHolder: ViewHolder): Int = {
    Callback.makeFlag(ItemTouchHelper.ACTION_STATE_DRAG, UP | DOWN)
  }
  override def onSwiped(viewHolder: ViewHolder, i: Int): Unit = {
    Log info "[init]"
    // nop
  }
  override def onMove(recyclerView: RecyclerView, holder: ViewHolder, target: ViewHolder): Boolean = {
    Log info "[init]"
    val from = holder.getAdapterPosition
    val to = target.getAdapterPosition
    recyclerView.getAdapter.notifyItemMoved(from, to)
    true
  }
}
