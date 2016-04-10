package x7c1.linen.modern.init.settings.preset

import android.support.v4.app.FragmentActivity
import android.support.v7.app.AlertDialog
import android.support.v7.widget.PopupMenu.OnMenuItemClickListener
import android.support.v7.widget.{PopupMenu, LinearLayoutManager}
import android.view.{MenuItem, Menu}
import x7c1.linen.glue.activity.ActivityControl
import x7c1.linen.glue.res.layout.{SettingSourceCopyRowItem, SettingSourceCopy, SettingChannelSourcesLayout, SettingChannelSourcesRow}
import x7c1.linen.glue.service.ServiceControl
import x7c1.linen.glue.service.ServiceLabel.Updater
import x7c1.linen.modern.accessor.preset.ClientAccount
import x7c1.linen.modern.accessor.{LinenOpenHelper, SettingSourceAccessorFactory}
import x7c1.linen.modern.display.settings.{ChannelSourcesSelected, OnSyncClickedListener, SourceMenuSelected, SourceRowAdapter}
import x7c1.linen.modern.init.updater.UpdaterMethods
import x7c1.wheat.ancient.context.ContextualFactory
import x7c1.wheat.ancient.resource.{ViewHolderProviderFactory, ViewHolderProvider}
import x7c1.wheat.macros.fragment.FragmentFactory
import x7c1.wheat.macros.intent.{IntentExpander, ServiceCaller}
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.Imports._
import x7c1.wheat.modern.resource.MetricsConverter

class PresetChannelSourcesDelegatee (
  activity: FragmentActivity with ActivityControl with ServiceControl,
  layout: SettingChannelSourcesLayout,
  dialogFactory: ContextualFactory[AlertDialog.Builder],
  copyLayoutFactory: ViewHolderProviderFactory[SettingSourceCopy],
  copyRowFactory: ViewHolderProviderFactory[SettingSourceCopyRowItem],
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
      channelId = event.channelId,
      viewHolderProvider = sourceRowProvider,
      onMenuSelected = new OnSourceMenuSelected(
        activity,
        dialogFactory, copyLayoutFactory, copyRowFactory ).showMenu,

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

class OnSourceMenuSelected(
  activity: FragmentActivity,
  dialogFactory: ContextualFactory[AlertDialog.Builder],
  copyLayoutFactory: ViewHolderProviderFactory[SettingSourceCopy],
  copyRowFactory: ViewHolderProviderFactory[SettingSourceCopyRowItem]
){

  def showMenu(event: SourceMenuSelected): Unit = {
    Log info s"[init]"
    val menu = new PopupMenu(activity, event.targetView)
    menu.getMenu.add(Menu.NONE, 123, 1, "Channels to copy source")
    menu setOnMenuItemClickListener new OnMenuItemClickListener {
      override def onMenuItemClick(item: MenuItem): Boolean = {
        if (item.getItemId == 123){
          createCopySourceDialog(event) showIn activity
        }
        true
      }
    }
    menu.show()
  }
  def createCopySourceDialog(event: SourceMenuSelected) = {
    FragmentFactory.create[CopySourceDialog] by
      new CopySourceDialog.Arguments(
        clientAccountId = event.clientAccountId,
        originalChannelId = event.channelId,
        originalSourceId = event.selectedSourceId,
        dialogFactory = dialogFactory,
        copyLayoutFactory = copyLayoutFactory,
        rowFactory = copyRowFactory
      )
  }
}
