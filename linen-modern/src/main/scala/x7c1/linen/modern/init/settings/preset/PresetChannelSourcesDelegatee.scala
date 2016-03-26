package x7c1.linen.modern.init.settings.preset

import android.app.Activity
import android.support.v7.widget.LinearLayoutManager
import x7c1.linen.glue.activity.ActivityControl
import x7c1.linen.glue.res.layout.{SettingChannelSourcesLayout, SettingChannelSourcesRow}
import x7c1.linen.glue.service.ServiceControl
import x7c1.linen.glue.service.ServiceLabel.Updater
import x7c1.linen.modern.accessor.{LinenOpenHelper, SettingSourceAccessorFactory}
import x7c1.linen.modern.display.settings.{OnSyncClickedListener, SourceRowAdapter}
import x7c1.linen.modern.init.updater.UpdaterMethods
import x7c1.wheat.ancient.resource.ViewHolderProvider
import x7c1.wheat.macros.intent.{IntentExpander, ServiceCaller}
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.Imports._


class PresetChannelSourcesDelegatee (
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
  def showSources(accountId: Long, channelId: Long): Unit = {
    Log info s"account:$accountId, channel:$channelId"
    val accessorFactory = new SettingSourceAccessorFactory(database, accountId)

    val a0 = accessorFactory create channelId
    Log info s"len:${a0.length}"

    layout.sourceList setAdapter new SourceRowAdapter(
      accessor = a0,
      viewHolderProvider = sourceRowProvider,
      onSyncClicked = onSyncClicked
    )
  }
  private def onSyncClicked = OnSyncClickedListener { event =>
    ServiceCaller.using[UpdaterMethods].
      startService(activity, activity getClassOf Updater){
        _ loadSource event.sourceId
      }
  }

}
