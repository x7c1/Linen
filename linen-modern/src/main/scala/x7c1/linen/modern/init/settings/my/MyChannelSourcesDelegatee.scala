package x7c1.linen.modern.init.settings.my

import android.app.Activity
import android.support.v7.widget.LinearLayoutManager
import x7c1.linen.glue.activity.ActivityControl
import x7c1.linen.glue.res.layout.{SettingChannelSourcesLayout, SettingChannelSourcesRow}
import x7c1.linen.glue.service.ServiceControl
import x7c1.linen.glue.service.ServiceLabel.Updater
import x7c1.linen.modern.accessor.preset.ClientAccount
import x7c1.linen.modern.accessor.{LinenOpenHelper, SettingSourceAccessorFactory}
import x7c1.linen.modern.display.settings.{SourceMenuSelected, ChannelSourcesSelected, OnSyncClickedListener, SourceRowAdapter}
import x7c1.linen.modern.init.updater.UpdaterMethods
import x7c1.wheat.ancient.resource.ViewHolderProvider
import x7c1.wheat.macros.intent.{IntentExpander, ServiceCaller}
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.Imports._
import x7c1.wheat.modern.resource.MetricsConverter

class MyChannelSourcesDelegatee (
  activity: Activity with ActivityControl with ServiceControl,
  layout: SettingChannelSourcesLayout,
  sourceRowProvider: ViewHolderProvider[SettingChannelSourcesRow] ){

  private lazy val database =
    new LinenOpenHelper(activity).getReadableDatabase

  def setup(): Unit = {
    layout.toolbar onClickNavigation { _ =>
      activity.finish()
    }
    val manager = new LinearLayoutManager(activity)
    layout.sourceList setLayoutManager manager

    IntentExpander executeBy activity.getIntent
  }
  def close(): Unit = {
    Log info "[init]"
    database.close()
  }
  def showSources(event: ChannelSourcesSelected): Unit = {
    Log info s"$event"

    val accessorFactory = new SettingSourceAccessorFactory(database, event.accountId)
    layout.sourceList setAdapter new SourceRowAdapter(
      accessor = accessorFactory create event.channelId,
      account = ClientAccount(event.accountId),
      viewHolderProvider = sourceRowProvider,
      onMenuSelected = new OnSourceMenuSelected().showMenu,
      onSyncClicked = onSyncClicked,
      metricsConverter = MetricsConverter(activity)
    )
    layout.toolbar setTitle event.channelName
  }
  private def onSyncClicked = OnSyncClickedListener { event =>
    ServiceCaller.using[UpdaterMethods].
      startService(activity, activity getClassOf Updater){
        _ loadSource event.sourceId
      }
  }
}

class OnSourceMenuSelected {
  def showMenu(event: SourceMenuSelected): Unit = {
    Log info s"[init]"
  }
}
