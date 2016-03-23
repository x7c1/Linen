package x7c1.linen.modern.init.unread

import java.lang.Math.min

import android.app.Activity
import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView.Adapter
import x7c1.linen.glue.activity.ActivityControl
import x7c1.linen.glue.activity.ActivityLabel.{CreateRecords, SettingChannels, SettingPresetChannels}
import x7c1.linen.glue.res.layout.{MenuRow, MenuRowLabel}
import x7c1.linen.modern.accessor.preset.ClientAccount
import x7c1.linen.modern.accessor.unread.ChannelLoaderEvent.{AccessorError, Done}
import x7c1.linen.modern.accessor.unread.{ChannelLoaderEvent, UnreadChannelAccessor, UnreadChannelLoader}
import x7c1.linen.modern.display.unread.MenuItemKind.{ChannelOrder, DevCreateDummies, MyChannels, NoChannel, PresetChannels, UnreadChannelMenu, UpdaterSchedule}
import x7c1.linen.modern.display.unread.{DrawerMenuLabelFactory, DrawerMenuRowAdapter, DrawerMenuTitleFactory, MenuItemKind, OnMenuItemClickListener}
import x7c1.linen.modern.init.settings.SettingChannelsDelegatee
import x7c1.linen.modern.init.settings.preset.PresetChannelsDelegatee
import x7c1.wheat.ancient.resource.ViewHolderProvider
import x7c1.wheat.macros.intent.IntentFactory
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.Imports._
import x7c1.wheat.modern.menu.{MenuItem, MenuItems, SingleMenuItem}

trait DrawerMenuInitializer {
  self: UnreadItemsDelegatee =>

  type OnChannelLoaded = Done => Unit

  def setupDrawerMenu(listener: OnChannelLoaded): Unit = {
    val manager = new LinearLayoutManager(layout.menuArea.getContext)
    layout.menuList setLayoutManager manager
    layout.menuArea setLayoutParams {
      val params = layout.menuArea.getLayoutParams
      val maxWidth = dipToPixel(320)
      val defaultWidth = displaySize.x - dipToPixel(65)
      params.width = min(maxWidth, defaultWidth)
      params
    }
    clientAccount foreach { account =>
      val loader = new UnreadChannelLoader(helper, account)
      val menuItems = createMenuItems(account, loader.accessor)
      val adapter = new DrawerMenuRowAdapter(menuItems)
      layout.menuList setAdapter adapter

      val task = loader.startLoading() map onLoad(adapter, listener)
      task.execute()
    }
  }
  private def onLoad(
    adapter: Adapter[_],
    listener: OnChannelLoaded): ChannelLoaderEvent => Unit = {

    case e: Done =>
      Log info s"[done]"
      layout.menuList runUi { _ => adapter.notifyDataSetChanged() }
      listener(e)
    case e: AccessorError =>
      Log error e.detail
  }

  private def createMenuItems(
    account: ClientAccount, accessor: UnreadChannelAccessor): MenuItems[MenuRow] = {

    val onClick = new OnMenuItemClick(activity, account.accountId)
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
  accountId: Long ) extends OnMenuItemClickListener {

  override def onClick(kind: MenuItemKind): Unit = kind match {
    case channel: UnreadChannelMenu =>
      Log info s"$kind, ${channel.channelId}, ${channel.body}"
    case _: NoChannel =>
      Log info s"$kind"
    case _: MyChannels =>
      Log info s"$kind"

      val intent = IntentFactory.using[SettingChannelsDelegatee].
        create(activity, activity getClassOf SettingChannels){
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
