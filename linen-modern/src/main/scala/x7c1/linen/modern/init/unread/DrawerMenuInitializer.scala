package x7c1.linen.modern.init.unread

import java.lang.Math.min

import android.app.Activity
import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import x7c1.linen.glue.activity.ActivityControl
import x7c1.linen.glue.activity.ActivityLabel.{CreateRecords, SettingChannels}
import x7c1.linen.modern.display.unread.MenuItemKind.{ChannelSources, Channels, DevCreateDummies, DevShowRecords, NoChannel, UpdaterSchedule}
import x7c1.linen.modern.display.unread.{MenuLabelFactory, DrawerMenuRowAdapter, MenuTitleFactory, MenuItemKind, MenuItemSeparator, MenuItemsBox, MenuItemsBoxes, OnMenuItemClickListener}
import x7c1.wheat.macros.logger.Log

trait DrawerMenuInitializer {
  self: UnreadItemsDelegatee =>

  def setupDrawerMenu(): Unit = {
    val manager = new LinearLayoutManager(layout.menuArea.getContext)
    layout.menuList setLayoutManager manager
    layout.menuList setAdapter new DrawerMenuRowAdapter(menuItemsBoxes)
    layout.menuArea setLayoutParams {
      val params = layout.menuArea.getLayoutParams
      val maxWidth = dipToPixel(320)
      val defaultWidth = displaySize.x - dipToPixel(65)
      params.width = min(maxWidth, defaultWidth)
      params
    }
  }
  protected def menuItemsBoxes = {
    val onClick = new OnMenuItemClick(activity)
    val title = new MenuTitleFactory(menuRowProviders.forTitle)
    val label = new MenuLabelFactory(onClick, menuRowProviders.forLabel)
    val separator = new MenuItemSeparator(menuRowProviders.forSeparator)

    MenuItemsBoxes(
      new MenuItemsBox(
        title of "Unread Channels",
        label of NoChannel("(all articles browsed)")
      ),
      separator,
      new MenuItemsBox(
        title of "Settings",
        label of Channels("Channels"),
        label of ChannelSources("Channel Sources"),
        label of UpdaterSchedule("Updater Schedule")
      ),
      separator,
      new MenuItemsBox(
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
