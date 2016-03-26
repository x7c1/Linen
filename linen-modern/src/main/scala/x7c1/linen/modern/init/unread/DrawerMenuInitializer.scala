package x7c1.linen.modern.init.unread

import java.lang.Math.min

import android.app.Activity
import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import x7c1.linen.glue.activity.ActivityControl
import x7c1.linen.glue.activity.ActivityLabel.{CreateRecords, SettingMyChannels, SettingPresetChannels}
import x7c1.linen.glue.res.layout.{MenuRow, MenuRowLabel}
import x7c1.linen.modern.accessor.preset.ClientAccount
import x7c1.linen.modern.accessor.unread.ChannelLoaderEvent.{AccessorError, Done}
import x7c1.linen.modern.accessor.unread.{ChannelLoaderEvent, UnreadChannelAccessor, UnreadChannelLoader}
import x7c1.linen.modern.display.settings.MyChannelSubscribeChanged
import x7c1.linen.modern.display.unread.MenuItemKind.{ChannelOrder, DevCreateDummies, MyChannels, NoChannel, PresetChannels, UnreadChannelMenu, UpdaterSchedule}
import x7c1.linen.modern.display.unread.{DrawerMenuLabelFactory, DrawerMenuRowAdapter, DrawerMenuTitleFactory, MenuItemKind, OnMenuItemClickListener}
import x7c1.linen.modern.init.settings.my.MyChannelsDelegatee
import x7c1.linen.modern.init.settings.preset.PresetChannelsDelegatee
import x7c1.wheat.ancient.resource.ViewHolderProvider
import x7c1.wheat.macros.intent.{IntentFactory, LocalBroadcastListener}
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.callback.CallbackTask
import x7c1.wheat.modern.decorator.Imports._
import x7c1.wheat.modern.menu.{MenuItem, MenuItems, SingleMenuItem}

trait DrawerMenuInitializer {
  self: UnreadItemsDelegatee =>

  def setupDrawerMenu(): Unit = {
    val manager = new LinearLayoutManager(layout.menuArea.getContext)
    layout.menuList setLayoutManager manager
    layout.menuArea updateLayoutParams { params =>
      val maxWidth = dipToPixel(320)
      val defaultWidth = displaySize.x - dipToPixel(65)
      params.width = min(maxWidth, defaultWidth)
    }
    channelLoader -> clientAccount match {
      case (Some(loader), Some(account)) =>
        layout.menuList setAdapter new DrawerMenuRowAdapter(
          items = createMenuItems(account, loader.accessor)
        )
        val task = loader.startLoading() flatMap onChannelLoaded map reader.onMenuLoaded
        task.execute()
      case _ =>
        Log error s"client not found"
    }
    onSubscribeMyChannel registerTo activity
  }
  def closeDrawerMenu(): Unit = {
    onSubscribeMyChannel unregisterFrom activity
  }
  protected lazy val channelLoader = clientAccount match {
    case Some(account) => Some(new UnreadChannelLoader(helper, account))
    case None => None
  }
  protected lazy val onSubscribeMyChannel =
    LocalBroadcastListener[MyChannelSubscribeChanged]{ event =>
      val task = channelLoader map (_.startLoading() flatMap onChannelLoaded)
      task foreach (_.execute())
    }

  protected def onChannelLoaded(event: ChannelLoaderEvent): CallbackTask[Done] = CallbackTask { f =>
    event match {
      case e: Done =>
        Log info s"[done]"
        layout.menuList runUi { _.getAdapter.notifyDataSetChanged() }
        f(e)
      case e: AccessorError =>
        Log error e.detail
    }
  }
  private def createMenuItems(
    account: ClientAccount, accessor: UnreadChannelAccessor): MenuItems[MenuRow] = {

    val onClick = new OnMenuItemClick(activity, account.accountId, reader.reloadChannel)
    val title = new DrawerMenuTitleFactory(menuRowProviders.forTitle)
    val label = new DrawerMenuLabelFactory(menuRowProviders.forLabel, onClick)
    val ----- = new SingleMenuItem(menuRowProviders.forSeparator)

    MenuItems(
      MenuItems(
        title of "Unread Channels",
        new UnreadChannelsMenu(menuRowProviders.forLabel, label, accessor)
      ),
      -----,
      MenuItems(
        title of "Settings",
        label of MyChannels("My Channels"),
        label of PresetChannels("Preset Channels"),
        label of ChannelOrder("Channel Order"),
        label of UpdaterSchedule("Updater Schedule")
      ),
      -----,
      MenuItems(
        title of "Developer Menu",
        label of DevCreateDummies("Create Records")
      )
    )
  }

}

class UnreadChannelsMenu(
  viewHolderProvider: ViewHolderProvider[MenuRowLabel],
  label: DrawerMenuLabelFactory,
  accessor: UnreadChannelAccessor) extends MenuItem[MenuRowLabel]{

  override def length = Math.max(accessor.length, 1)

  override def findItemAt(position: Int) = {
    accessor.length match {
      case 0 => Some(label of NoChannel("(no unread channels)"))
      case _ => accessor findAt position map { channel =>
        label of UnreadChannelMenu(
          channelId = channel.channelId,
          body = channel.name )
      }
    }
  }
  override def viewHolderProviders = Seq(viewHolderProvider)
}

class OnMenuItemClick(
  activity: Activity with ActivityControl,
  accountId: Long,
  onUnreadChannelSelected: UnreadChannelMenu => Unit) extends OnMenuItemClickListener {

  override def onClick(kind: MenuItemKind): Unit = kind match {
    case channel: UnreadChannelMenu =>
      Log info s"$kind, ${channel.channelId}, ${channel.body}"
      onUnreadChannelSelected(channel)
    case _: NoChannel =>
      Log info s"$kind"
    case _: MyChannels =>
      Log info s"$kind"

      val intent = IntentFactory.using[MyChannelsDelegatee].
        create(activity, activity getClassOf SettingMyChannels){
          _.showMyChannels(accountId)
        }
      activity startActivityBy intent

//      activity startActivityBy new Intent(
//        activity, activity getClassOf SettingChannels)

    case _: PresetChannels =>
      Log info s"$kind"

      val intent = IntentFactory.using[PresetChannelsDelegatee].
        create(activity, activity getClassOf SettingPresetChannels){
          _.showPresetChannels(accountId)
        }

      activity startActivityBy intent

    case _: ChannelOrder =>
      Log info s"$kind"
    case _: UpdaterSchedule =>
      Log info s"$kind"
    case _: DevCreateDummies =>
      activity startActivityBy new Intent(
        activity, activity getClassOf CreateRecords)
  }
}
