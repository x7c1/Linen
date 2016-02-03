package x7c1.linen.modern.init

import java.lang.Math.min

import android.app.Activity
import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import x7c1.linen.glue.activity.ActivityControl
import x7c1.linen.glue.activity.ActivityLabel.{CreateRecords, SettingChannels}
import x7c1.linen.modern.display.DrawerMenuItemKind.{ChannelSources, Channels, DevCreateDummies, DevShowRecords, NoChannel, UpdaterSchedule}
import x7c1.linen.modern.display.{DrawerMenuItemFactory, DrawerMenuItemKind, DrawerMenuLabelFactory, DrawerMenuRowAdapter, MenuItemsBox, MenuItemsBoxes, OnDrawerMenuClickListener}
import x7c1.wheat.macros.logger.Log

trait DrawerMenuInitializer {
  self: ContainerInitializer =>

  def setupDrawerMenu(): Unit = {
    val manager = new LinearLayoutManager(layout.menuArea.getContext)
    layout.menuList setLayoutManager manager
    layout.menuList setAdapter new DrawerMenuRowAdapter(
      menuLabelProvider,
      menuItemProvider,
      menuItemsBoxes
    )
    layout.menuArea setLayoutParams {
      val params = layout.menuArea.getLayoutParams
      val maxWidth = dipToPixel(320)
      val defaultWidth = displaySize.x - dipToPixel(65)
      params.width = min(maxWidth, defaultWidth)
      params
    }
  }
  protected def menuItemsBoxes = {
    val onClick = new OnDrawerMenuClick(activity)
    val label = new DrawerMenuLabelFactory(menuLabelProvider)
    val item = new DrawerMenuItemFactory(onClick, menuItemProvider)

    MenuItemsBoxes(
      new MenuItemsBox(
        label of "Unread Channels",
        item of NoChannel("(all articles browsed)")
      ),
      new MenuItemsBox(
        label of "Settings",
        item of Channels("Channels"),
        item of ChannelSources("Channel Sources"),
        item of UpdaterSchedule("Updater Schedule")
      ),
      new MenuItemsBox(
        label of "Developer Menu",
        item of DevCreateDummies("Create Records"),
        item of DevShowRecords("Show Records")
      )
    )
  }
}

class OnDrawerMenuClick(
  activity: Activity with ActivityControl) extends OnDrawerMenuClickListener {

  override def onClick(kind: DrawerMenuItemKind): Unit = kind match {
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
