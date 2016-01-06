package x7c1.linen.modern.init

import java.lang.Math.min

import android.app.Activity
import android.graphics.Point
import android.support.v7.widget.LinearLayoutManager
import x7c1.linen.glue.activity.ActivityLabel.SettingChannels
import x7c1.linen.glue.activity.ActivityStarter
import x7c1.linen.glue.res.layout.{MainLayout, MenuRowItem, MenuRowLabel}
import x7c1.linen.modern.display.DrawerMenuItemKind.{ChannelSources, Channels, CrawlerSchedule, NoChannel}
import x7c1.linen.modern.display.{DrawerMenuItemFactory, DrawerMenuItemKind, DrawerMenuLabelFactory, DrawerMenuRowAdapter, MenuItemsBox, MenuItemsBoxes, OnDrawerMenuClickListener}
import x7c1.wheat.ancient.resource.ViewHolderProvider
import x7c1.wheat.macros.logger.Log

trait DrawerMenuInitializer {
  def activity: Activity with ActivityStarter
  def layout: MainLayout
  def menuLabelProvider: ViewHolderProvider[MenuRowLabel]
  def menuItemProvider: ViewHolderProvider[MenuRowItem]
  def displaySize: Point
  def dipToPixel(dip: Int): Int

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
        label of "Unread channels",
        item of NoChannel("(all articles browsed)")
      ),
      new MenuItemsBox(
        label of "Settings",
        item of Channels("Channels"),
        item of ChannelSources("Channel sources"),
        item of CrawlerSchedule("Crawler schedule")
      )
    )
  }
}

class OnDrawerMenuClick(starter: ActivityStarter) extends OnDrawerMenuClickListener {
  override def onClick(kind: DrawerMenuItemKind): Unit = kind match {
    case _: NoChannel =>
      Log info s"$kind"
    case _: Channels =>
      Log info s"$kind"
      starter transitTo SettingChannels
    case _: ChannelSources =>
      Log info s"$kind"
    case _: CrawlerSchedule =>
      Log info s"$kind"
  }
}
