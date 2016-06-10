package x7c1.linen.modern.init.settings.order

import android.app.Activity
import android.support.v7.widget.LinearLayoutManager
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
