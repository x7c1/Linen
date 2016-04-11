package x7c1.linen.modern.init.settings.source

import android.support.v4.app.FragmentActivity
import android.support.v7.app.AlertDialog
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.PopupMenu.OnMenuItemClickListener
import android.view.{MenuItem, Menu}
import x7c1.linen.glue.res.layout.{SettingSourceAttachRowItem, SettingSourceAttach}
import x7c1.linen.modern.display.settings.SourceMenuSelected
import x7c1.linen.modern.init.settings.preset.AttachSourceDialog
import x7c1.wheat.ancient.context.ContextualFactory
import x7c1.wheat.ancient.resource.ViewHolderProviderFactory
import x7c1.wheat.macros.fragment.FragmentFactory


class OnSourceMenuSelected(
  activity: FragmentActivity,
  dialogFactory: ContextualFactory[AlertDialog.Builder],
  attachLayoutFactory: ViewHolderProviderFactory[SettingSourceAttach],
  attachRowFactory: ViewHolderProviderFactory[SettingSourceAttachRowItem]
){
  def showMenu(event: SourceMenuSelected): Unit = {
    val menu = new PopupMenu(activity, event.targetView)
    menu.getMenu.add(Menu.NONE, 123, 1, "Attached channels")
    menu setOnMenuItemClickListener new OnMenuItemClickListener {
      override def onMenuItemClick(item: MenuItem): Boolean = {
        if (item.getItemId == 123){
          createAttachSourceDialog(event) showIn activity
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
