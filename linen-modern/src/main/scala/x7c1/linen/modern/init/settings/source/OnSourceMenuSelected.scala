package x7c1.linen.modern.init.settings.source

import android.support.v4.app.FragmentActivity
import android.support.v7.app.AlertDialog
import x7c1.linen.glue.res.layout.{SettingSourceAttach, SettingSourceAttachRowItem}
import x7c1.linen.glue.service.ServiceControl
import x7c1.linen.modern.display.settings.SourceMenuSelected
import x7c1.linen.modern.init.settings.preset.AttachSourceDialog
import x7c1.linen.scene.loader.crawling.QueueingService
import x7c1.wheat.ancient.context.ContextualFactory
import x7c1.wheat.ancient.resource.ViewHolderProviderFactory
import x7c1.wheat.macros.fragment.FragmentFactory
import x7c1.wheat.modern.menu.popup.{PopupMenuBox, PopupMenuItem}


class OnSourceMenuSelected(
  activity: FragmentActivity with ServiceControl,
  dialogFactory: ContextualFactory[AlertDialog.Builder],
  attachLayoutFactory: ViewHolderProviderFactory[SettingSourceAttach],
  attachRowFactory: ViewHolderProviderFactory[SettingSourceAttachRowItem]
){
  def showMenu(event: SourceMenuSelected): Unit = {

    def toAttach = PopupMenuItem("Attached my channels"){ _ =>
      createAttachSourceDialog(event) showIn activity
    }
    def toLoad = PopupMenuItem("Load now"){ _ =>
      QueueingService(activity).loadSource(event.selectedSourceId)
    }
    val items = Seq(
      toAttach,
      toLoad
    )
    PopupMenuBox(activity, event.targetView).show(items:_*)
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
