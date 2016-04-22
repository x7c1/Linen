package x7c1.linen.scene.channel.menu

import android.app.Activity
import x7c1.linen.glue.service.ServiceControl
import x7c1.linen.glue.service.ServiceLabel.Updater
import x7c1.linen.scene.updater.UpdaterMethods
import x7c1.wheat.macros.intent.ServiceCaller
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.menu.popup.{PopupMenuBox, PopupMenuItem}

class OnChannelMenuSelected(
  activity: Activity with ServiceControl,
  accountId: Long) extends OnMenuSelectedListener {

  override def onMenuSelected(e: MenuSelected) = {
    val items = Seq(
      itemToLoadSources(e.channelId)
    )
    PopupMenuBox(activity, e.targetView, items).show()
  }
  private def itemToLoadSources(channelId: Long) =
    PopupMenuItem("Load all sources"){ _ =>
      Log info s"[init] channel:$channelId"
      ServiceCaller.using[UpdaterMethods].
        startService(activity, activity getClassOf Updater){
          _ loadChannelSources (channelId, accountId)
        }
    }
}
