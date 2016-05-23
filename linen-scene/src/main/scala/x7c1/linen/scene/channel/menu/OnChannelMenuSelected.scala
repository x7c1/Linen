package x7c1.linen.scene.channel.menu

import android.app.Activity
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.glue.service.ServiceControl
import x7c1.linen.repository.channel.my.MyChannel
import x7c1.linen.repository.channel.preset.SettingPresetChannel
import x7c1.linen.scene.loader.crawling.QueueingService
import x7c1.wheat.macros.intent.LocalBroadcaster
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.formatter.ThrowableFormatter
import x7c1.wheat.modern.menu.popup.PopupMenuItem


object OnChannelMenuSelected {
  def forMyChannel(
    activity: Activity with ServiceControl,
    accountId: Long,
    helper: DatabaseHelper,
    onDeleted: MyChannelDeleted => Unit ): OnMenuSelectedListener[MyChannel] = {

    Log info s"[init]"
    OnMenuSelectedListener.create(activity){ event =>
      val factory = new MenuItemFactory(activity, accountId)
      Seq(
        factory.toLoadSources(event.channelId),
        factory.toDeleteChannel(helper, event.channel, onDeleted)
      )
    }
  }

  def forPresetChannel(
    activity: Activity with ServiceControl,
    accountId: Long): OnMenuSelectedListener[SettingPresetChannel] = {

    OnMenuSelectedListener.create(activity){ event =>
      val factory = new MenuItemFactory(activity, accountId)
      Seq(
        factory.toLoadSources(event.channelId)
      )
    }
  }
}

class MenuItemFactory(
  activity: Activity with ServiceControl,
  accountId: Long){

  def toLoadSources(channelId: Long): PopupMenuItem =
    PopupMenuItem("Load all sources"){ _ =>
      Log info s"[init] channel:$channelId"
      QueueingService(activity).loadChannelSources(channelId, accountId)
    }

  def toDeleteChannel(
    helper: DatabaseHelper,
    channel: MyChannel,
    onDeleted: MyChannelDeleted => Unit ): PopupMenuItem = {

    PopupMenuItem("Delete this channel"){ _ =>
      helper.writable delete channel match {
        case Left(error) =>
          Log error (ThrowableFormatter format error){"[failed]"}
        case Right(_) =>
          val event = MyChannelDeleted(channel)
          onDeleted(event)
          LocalBroadcaster(event) dispatchFrom activity
      }
    }
  }
}

case class MyChannelDeleted(channel: MyChannel)
