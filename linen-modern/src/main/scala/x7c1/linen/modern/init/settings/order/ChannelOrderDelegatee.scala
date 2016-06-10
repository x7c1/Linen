package x7c1.linen.modern.init.settings.order

import android.app.Activity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.helper.ItemTouchHelper
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.database.struct.HasAccountId
import x7c1.linen.glue.activity.ActivityControl
import x7c1.linen.glue.res.layout.{SettingChannelOrderLayout, SettingChannelOrderRowItem}
import x7c1.linen.glue.service.ServiceControl
import x7c1.linen.repository.channel.subscribe.SubscribedChannel
import x7c1.wheat.lore.resource.AdapterDelegatee
import x7c1.wheat.macros.intent.IntentExpander
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.database.selector.presets.ClosableSequenceLoader.{Done, SqlError}
import x7c1.wheat.modern.decorator.Imports.{toRichTextView, toRichToolbar, toRichView}
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
    val touchHelper = new ItemTouchHelper(loader.callback)
    layout.channelList setLayoutManager new LinearLayoutManager(activity)
    layout.channelList setAdapter new ChannelOrderRowAdapter(
      delegatee = AdapterDelegatee.create(providers, loader.sequence),
      onDragStart = holder => touchHelper startDrag holder
    )
    touchHelper attachToRecyclerView layout.channelList
    layout.channelList addItemDecoration touchHelper

    IntentExpander executeBy activity.getIntent
  }
  def onDestroy(): Unit = {
    Log info s"[init]"
    helper.close()
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
  private lazy val helper = {
    new DatabaseHelper(activity)
  }
  private lazy val loader =
    new SequenceLoaderForDragging[HasAccountId, SubscribedChannel](
      db = helper.getReadableDatabase,
      onStart = {
        case (row: SettingChannelOrderRowItem, sequence) =>
          Log info s"start:${row.name.text}"
          // todo: change color of row being dragged
      },
      onFinish = {
        case (row: SettingChannelOrderRowItem, sequence) =>
          Log info s"[finish]"
          // todo: save updated order
          sequence.toSeq foreach { channel =>
            Log info s"${channel.name}"
          }
      }
    )
}
