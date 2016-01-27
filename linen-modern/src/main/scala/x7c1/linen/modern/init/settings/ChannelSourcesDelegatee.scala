package x7c1.linen.modern.init.settings

import android.app.Activity
import android.support.v7.widget.LinearLayoutManager
import x7c1.linen.glue.activity.ActivityControl
import x7c1.linen.glue.res.layout.{SettingChannelSourcesRow, SettingChannelSourcesLayout}
import x7c1.linen.modern.accessor.{SettingSourceAccessorFactory, LinenOpenHelper}
import x7c1.linen.modern.display.settings.SourceRowAdapter
import x7c1.wheat.ancient.resource.ViewHolderProvider
import x7c1.wheat.macros.intent.IntentExpander
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.Imports._

class ChannelSourcesDelegatee (
  activity: Activity with ActivityControl,
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

    layout.sourceList setAdapter new SourceRowAdapter(
      accessor = accessorFactory create channelId,
      viewHolderProvider = sourceRowProvider
    )
  }
}
