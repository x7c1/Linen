package x7c1.linen.modern.init.unread

import java.lang.Math.min

import android.app.Activity
import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import x7c1.linen.glue.activity.ActivityControl
import x7c1.linen.glue.activity.ActivityLabel.{CreateRecords, SettingLoaderSchedule, SettingMyChannels, SettingPresetChannels}
import x7c1.linen.glue.res.layout.{MenuRow, MenuRowLabel}
import x7c1.linen.modern.display.unread.MenuItemKind.{ChannelOrder, DevCreateDummies, LoaderSchedule, MyChannels, NoChannel, PresetChannels, UnreadChannelMenu}
import x7c1.linen.modern.display.unread.{DrawerMenuLabelFactory, DrawerMenuRowAdapter, DrawerMenuTitleFactory, MenuItemKind, OnMenuItemClickListener}
import x7c1.linen.modern.init.settings.my.MyChannelsDelegatee
import x7c1.linen.modern.init.settings.preset.PresetChannelsDelegatee
import x7c1.linen.modern.init.settings.schedule.LoaderSchedulesDelegatee
import x7c1.linen.repository.account.ClientAccount
import x7c1.linen.repository.channel.unread.selector.UnreadChannelSelector
import x7c1.linen.repository.channel.unread.{ChannelSelectable, UnreadChannel}
import x7c1.wheat.ancient.resource.ViewHolderProvider
import x7c1.wheat.macros.intent.IntentFactory
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.callback.CallbackTask.task
import x7c1.wheat.modern.decorator.Imports._
import x7c1.wheat.modern.menu.{MenuItem, MenuItems, SingleMenuItem}
import x7c1.wheat.modern.sequence.Sequence

trait DrawerMenuInitializer {
  self: UnreadItemsDelegatee =>

  def setupDrawerMenu(): Unit = {
    val manager = new LinearLayoutManager(layout.menuArea.getContext)
    layout.menuList setLayoutManager manager
    layout.menuArea updateLayoutParams { params =>
      val maxWidth = converter dipToPixel 320
      val defaultWidth = displaySize.x - converter.dipToPixel(65)
      params.width = min(maxWidth, defaultWidth)
    }
    clientAccount match {
      case Some(account) =>
        layout.menuList setAdapter new DrawerMenuRowAdapter(
          items = createMenuItems(account, channelLoader.sequence)
        )
        channelLoader.startLoading(account).
          flatMap(onChannelSubscriptionChanged.notifyAdapter).
          map(reader.onMenuLoaded).
          execute()

      case _ =>
        Log error s"client not found"
    }
    onChannelSubscriptionChanged registerTo activity
  }
  def closeDrawerMenu(): Unit = {
    onChannelSubscriptionChanged unregisterFrom activity
  }
  protected lazy val channelLoader = UnreadChannelSelector.createLoader(helper)

  protected lazy val onChannelSubscriptionChanged =
    new OnChannelSubscriptionChanged(layout, channelLoader)

  private def createMenuItems(
    account: ClientAccount, accessor: Sequence[UnreadChannel]): MenuItems[MenuRow] = {

    val onClick = new OnMenuItemClick(activity, account.accountId, onChannelSelected)
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
        label of LoaderSchedule("Loader Schedules")
      ),
      -----,
      MenuItems(
        title of "Developer Menu",
        label of DevCreateDummies("Create Records")
      )
    )
  }
  def onChannelSelected[A: ChannelSelectable](channel: A) = {
    val tasks = for {
      _ <- container.fadeOut()
      _ <- task { reader reloadChannel channel }
    } yield {}

    tasks.execute()
  }

}

class UnreadChannelsMenu(
  viewHolderProvider: ViewHolderProvider[MenuRowLabel],
  label: DrawerMenuLabelFactory,
  accessor: Sequence[UnreadChannel]) extends MenuItem[MenuRowLabel]{

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
      activity startActivity intent

//      activity startActivityBy new Intent(
//        activity, activity getClassOf SettingChannels)

    case _: PresetChannels =>
      Log info s"$kind"

      val intent = IntentFactory.using[PresetChannelsDelegatee].
        create(activity, activity getClassOf SettingPresetChannels){
          _.showPresetChannels(accountId)
        }

      activity startActivity intent

    case _: ChannelOrder =>
      Log info s"$kind"
    case _: LoaderSchedule =>
      Log info s"$kind"
      activity startActivity IntentFactory.using[LoaderSchedulesDelegatee].
        create(activity, activity getClassOf SettingLoaderSchedule){
          _ setupFor accountId
        }

    case _: DevCreateDummies =>
      activity startActivity new Intent(
        activity, activity getClassOf CreateRecords)
  }
}
