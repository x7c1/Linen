package x7c1.linen.modern.init

import java.lang.Math.min

import android.graphics.Point
import android.support.v7.widget.LinearLayoutManager
import x7c1.linen.glue.res.layout.{MainLayout, MenuRowItem, MenuRowLabel}
import x7c1.linen.modern.display.DrawerMenuItemKind.{CrawlerSchedule, Lists, NoList, Sources}
import x7c1.linen.modern.display.{DrawerMenuItemFactory, DrawerMenuItemKind, DrawerMenuLabelFactory, DrawerMenuRowAdapter, MenuItemsBox, MenuItemsBoxes, OnDrawerMenuClickListener}
import x7c1.wheat.ancient.resource.ViewHolderProvider
import x7c1.wheat.macros.logger.Log

trait DrawerMenuInitializer {
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
    val onClick = new OnDrawerMenuClick
    val label = new DrawerMenuLabelFactory(menuLabelProvider)
    val item = new DrawerMenuItemFactory(onClick, menuItemProvider)

    MenuItemsBoxes(
      new MenuItemsBox(
        label of "Unread lists",
        item of NoList("(unread article not found)")
      ),
      new MenuItemsBox(
        label of "Settings",
        item of Lists("Lists"),
        item of Sources("Sources"),
        item of CrawlerSchedule("Crawler schedule")
      )
    )
  }
}

class OnDrawerMenuClick extends OnDrawerMenuClickListener {
  override def onClick(kind: DrawerMenuItemKind): Unit = kind match {
    case _: NoList =>
      Log info s"$kind"
    case _: Lists =>
      Log info s"$kind"
    case _: Sources =>
      Log info s"$kind"
    case _: CrawlerSchedule =>
      Log info s"$kind"
  }
}
