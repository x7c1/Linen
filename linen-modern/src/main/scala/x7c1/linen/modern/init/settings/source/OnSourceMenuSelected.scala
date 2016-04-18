package x7c1.linen.modern.init.settings.source

import android.support.v4.app.FragmentActivity
import android.support.v7.app.AlertDialog
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.PopupMenu.OnMenuItemClickListener
import android.view.{Menu, MenuItem}
import x7c1.linen.glue.res.layout.{SettingSourceAttach, SettingSourceAttachRowItem}
import x7c1.linen.glue.service.ServiceControl
import x7c1.linen.glue.service.ServiceLabel.Updater
import x7c1.linen.modern.display.settings.SourceMenuSelected
import x7c1.linen.modern.init.settings.preset.AttachSourceDialog
import x7c1.linen.modern.init.updater.UpdaterMethods
import x7c1.wheat.ancient.context.ContextualFactory
import x7c1.wheat.ancient.resource.ViewHolderProviderFactory
import x7c1.wheat.macros.fragment.FragmentFactory
import x7c1.wheat.macros.intent.ServiceCaller
import x7c1.wheat.macros.logger.Log


class OnSourceMenuSelected(
  activity: FragmentActivity with ServiceControl,
  dialogFactory: ContextualFactory[AlertDialog.Builder],
  attachLayoutFactory: ViewHolderProviderFactory[SettingSourceAttach],
  attachRowFactory: ViewHolderProviderFactory[SettingSourceAttachRowItem]
){
  def showMenu(event: SourceMenuSelected): Unit = {
    val menu = new PopupMenu(activity, event.targetView)
    menu.getMenu.add(Menu.NONE, 123, 1, "Attached my channels")
    menu.getMenu.add(1, 234, 1, "Load now")
    menu setOnMenuItemClickListener new OnMenuItemClickListener {
      override def onMenuItemClick(item: MenuItem): Boolean = {
        item.getItemId match {
          case 123 =>
            createAttachSourceDialog(event) showIn activity
          case 234 =>
            ServiceCaller.using[UpdaterMethods].
              startService(activity, activity getClassOf Updater){
                _ loadSource event.selectedSourceId
              }
          case x => Log error s"unknown menu-id:$x"
        }
        true
      }
    }
    menu.show()
  }
  def createAttachSourceDialog(event: SourceMenuSelected) = {
    FragmentFactory.create[AttachSourceDialog] by
      new AttachSourceDialog.Arguments(
        clientAccountId = event.clientAccountId,
        originalChannelId = event.channelId,
        originalSourceId = event.selectedSourceId,
        dialogFactory = dialogFactory,
        attachLayoutFactory = attachLayoutFactory,
        rowFactory = attachRowFactory
      )
  }
}
