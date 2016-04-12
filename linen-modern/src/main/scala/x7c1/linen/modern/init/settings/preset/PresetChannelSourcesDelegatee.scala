package x7c1.linen.modern.init.settings.preset

import android.support.v4.app.FragmentActivity
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import x7c1.linen.database.LinenOpenHelper
import x7c1.linen.domain.account.ClientAccount
import x7c1.linen.glue.activity.ActivityControl
import x7c1.linen.glue.res.layout.{SettingChannelSourcesLayout, SettingChannelSourcesRow, SettingSourceAttach, SettingSourceAttachRowItem}
import x7c1.linen.glue.service.ServiceControl
import x7c1.linen.glue.service.ServiceLabel.Updater
import x7c1.linen.modern.accessor.SettingSourceAccessorFactory
import x7c1.linen.modern.display.settings.{ChannelSourcesSelected, OnSyncClickedListener, SourceRowAdapter}
import x7c1.linen.modern.init.settings.source.OnSourceMenuSelected
import x7c1.linen.modern.init.updater.UpdaterMethods
import x7c1.wheat.ancient.context.ContextualFactory
import x7c1.wheat.ancient.resource.{ViewHolderProvider, ViewHolderProviderFactory}
import x7c1.wheat.macros.intent.{IntentExpander, ServiceCaller}
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.Imports._
import x7c1.wheat.modern.resource.MetricsConverter

class PresetChannelSourcesDelegatee (
  activity: FragmentActivity with ActivityControl with ServiceControl,
  layout: SettingChannelSourcesLayout,
  dialogFactory: ContextualFactory[AlertDialog.Builder],
  attachLayoutFactory: ViewHolderProviderFactory[SettingSourceAttach],
  attachRowFactory: ViewHolderProviderFactory[SettingSourceAttachRowItem],
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
    val accessorFactory = new SettingSourceAccessorFactory(database, event.accountId)
    layout.sourceList setAdapter new SourceRowAdapter(
      accessor = accessorFactory create event.channelId,
      account = ClientAccount(event.accountId),
      channelId = event.channelId,
      viewHolderProvider = sourceRowProvider,
      onMenuSelected = new OnSourceMenuSelected(
        activity,
        dialogFactory, attachLayoutFactory, attachRowFactory ).showMenu,

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
