package x7c1.linen.modern.init.unread

import java.lang.Math.min

import android.app.Activity
import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import x7c1.linen.glue.activity.ActivityControl
import x7c1.linen.glue.activity.ActivityLabel.{CreateRecords, SettingChannels}
import x7c1.linen.glue.res.layout.MenuRow
import x7c1.linen.modern.display.unread.MenuItemKind.{ChannelSources, Channels, DevCreateDummies, DevShowRecords, NoChannel, UpdaterSchedule}
import x7c1.linen.modern.display.unread.{DrawerMenuLabelFactory, DrawerMenuRowAdapter, DrawerMenuSeparator, DrawerMenuTitleFactory, MenuItemKind, OnMenuItemClickListener}
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.menu.MenuItems

trait DrawerMenuInitializer {
  self: UnreadItemsDelegatee =>

  def setupDrawerMenu(): Unit = {
    val manager = new LinearLayoutManager(layout.menuArea.getContext)
    layout.menuList setLayoutManager manager
    layout.menuList setAdapter new DrawerMenuRowAdapter(menuItems)
    layout.menuArea setLayoutParams {
      val params = layout.menuArea.getLayoutParams
      val maxWidth = dipToPixel(320)
      val defaultWidth = displaySize.x - dipToPixel(65)
      params.width = min(maxWidth, defaultWidth)
      params
    }
  }

  protected def menuItems: MenuItems[MenuRow] = {
    val onClick = new OnMenuItemClick(activity)
    val title = new DrawerMenuTitleFactory(menuRowProviders.forTitle)
    val label = new DrawerMenuLabelFactory(menuRowProviders.forLabel, onClick)
    val ----- = new DrawerMenuSeparator(menuRowProviders.forSeparator)

    MenuItems(
      MenuItems(
        title of "Unread Channels",
        label of NoChannel("(all articles browsed)")
      ),
      -----,
      MenuItems(
        title of "Settings",
        label of Channels("Channels"),
        label of ChannelSources("Channel Sources"),
        label of UpdaterSchedule("Updater Schedule")
      ),
      -----,
      MenuItems(
        title of "Developer Menu",
        label of DevCreateDummies("Create Records"),
        label of DevShowRecords("Show Records")
      )
    )
  }

}

class OnMenuItemClick(
  activity: Activity with ActivityControl) extends OnMenuItemClickListener {

  override def onClick(kind: MenuItemKind): Unit = kind match {
    case _: NoChannel =>
      Log info s"$kind"
    case _: Channels =>
      Log info s"$kind"

      activity startActivityBy new Intent(
        activity, activity getClassOf SettingChannels)

    case _: ChannelSources =>
      Log info s"$kind"
    case _: UpdaterSchedule =>
      Log info s"$kind"

    case _: DevShowRecords =>
      Log info s"$kind"
    case _: DevCreateDummies =>
      activity startActivityBy new Intent(
        activity, activity getClassOf CreateRecords)
  }
}
