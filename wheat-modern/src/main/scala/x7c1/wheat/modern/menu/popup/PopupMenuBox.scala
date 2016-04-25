package x7c1.wheat.modern.menu.popup

import android.content.Context
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.PopupMenu.OnMenuItemClickListener
import android.view.{MenuItem, Menu, View}
import x7c1.wheat.macros.logger.Log

class PopupMenuBox private (
  context: Context,
  view: View,
  items: Seq[PopupMenuItem]){

  def show() = {
    val menu = new PopupMenu(context, view)
    val pairs = items.zipWithIndex
    pairs foreach {
      case (item, index) =>
        menu.getMenu.add(Menu.NONE, index, index, item.title)
    }
    menu setOnMenuItemClickListener toListener(pairs)
    menu.show()
  }
  private def toListener(pairs: Seq[(PopupMenuItem, Int)]) =
    new OnMenuItemClickListener {
      override def onMenuItemClick(menuItem: MenuItem): Boolean = {
        pairs collectFirst {
          case (item, index) if menuItem.getItemId == index => item
        } match {
          case Some(item) => item.onTapped onItemTapped MenuItemTappedEvent()
          case None => Log error s"unknown menu-id:${menuItem.getItemId}"
        }
        true
      }
    }
}
object PopupMenuBox {
  def apply(
    context: Context,
    view: View,
    items: Seq[PopupMenuItem]): PopupMenuBox = {

    new PopupMenuBox(context, view, items)
  }
  def apply(context: Context, view: View): MenuHolder = {
    new MenuHolder(context, view)
  }
  class MenuHolder (context: Context, view: View){
    def show(items: PopupMenuItem*): Unit = {
      new PopupMenuBox(context, view, items).show()
    }
  }
}
case class PopupMenuItem(
  title: String,
  onTapped: OnPopupMenuItemTapped
)
object PopupMenuItem {
  def apply(title: String)(f: MenuItemTappedEvent => Unit): PopupMenuItem = {
    val listener = new OnPopupMenuItemTapped {
      override def onItemTapped(event: MenuItemTappedEvent): Unit = f(event)
    }
    PopupMenuItem(title, listener)
  }
}

trait OnPopupMenuItemTapped {
  def onItemTapped(event: MenuItemTappedEvent): Unit
}

case class MenuItemTappedEvent()
