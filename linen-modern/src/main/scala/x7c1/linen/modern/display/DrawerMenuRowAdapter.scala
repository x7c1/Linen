package x7c1.linen.modern.display

import android.support.v7.widget.RecyclerView.Adapter
import android.view.ViewGroup
import x7c1.linen.glue.res.layout.{MenuRow, MenuRowLabel, MenuRowItem}
import x7c1.wheat.ancient.resource.ViewHolderProvider

import scala.annotation.tailrec

class DrawerMenuRowAdapter(
  menuLabelProvider: ViewHolderProvider[MenuRowLabel],
  menuItemProvider: ViewHolderProvider[MenuRowItem] ) extends Adapter[MenuRow]{

  private lazy val providers = new ViewHolderProviders[MenuRow](
    menuLabelProvider,
    menuItemProvider
  )

  private val boxes = MenuItemsBoxes(Seq(
    MenuItemsBox(MenuLabel("Unread lists", menuLabelProvider.layoutId), Seq(
      MenuItem("(empty)", menuItemProvider.layoutId)
    )),
    MenuItemsBox(MenuLabel("Settings", menuLabelProvider.layoutId), Seq(
      MenuItem("Lists", menuItemProvider.layoutId),
      MenuItem("Sources", menuItemProvider.layoutId)
    ))
  ))

  override def getItemCount: Int = boxes.count

  override def onBindViewHolder(row: MenuRow, position: Int): Unit = {
    row match {
      case holder: MenuRowItem =>
        boxes findItemAt position foreach { case (x: MenuItem) =>
          holder.text setText s"item $position ${x.body}"
        }
      case holder: MenuRowLabel =>
        boxes findItemAt position foreach { case (x: MenuLabel) =>
          holder.text setText s"label $position ${x.body}"
        }
    }
  }

  override def getItemViewType(position: Int): Int = {
    boxes findItemAt position map (_.viewType) getOrElse {
      throw new IllegalArgumentException(s"invalid position: $position")
    }
  }

  override def onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuRow = {
    providers get viewType inflateOn parent
  }
}

trait Menu {
  def viewType: Int
}

case class MenuLabel(body: String, viewType: Int) extends Menu

case class MenuItem(body: String, viewType: Int) extends Menu

case class MenuItemsBox(label: MenuLabel, items: Seq[MenuItem]){

  def length: Int = 1 + items.size

  def itemAt(position: Int): Menu = position match {
    case 0 => label
    case n => items(n - 1)
  }
}

case class MenuItemsBoxes(boxes: Seq[MenuItemsBox]){
  require(boxes.nonEmpty, "empty boxes")

  def count: Int = boxes.foldLeft(0){_ + _.length}

  def findItemAt(position: Int): Option[Menu] = {
    @tailrec
    def loop(boxes: Seq[MenuItemsBox], prev: Int): Option[(MenuItemsBox, Int)] = {
      boxes match {
        case x +: xs => x.length + prev match {
          case sum if sum > position => Some(x -> prev)
          case sum => loop(xs, sum)
        }
        case Seq() => None
      }
    }
    loop(boxes, 0) map { case (x, prev) => x.itemAt(position - prev) }
  }
}

private class ViewHolderProviders[A](providers: ViewHolderProvider[_ <: A]*){
  def get(viewType: Int): ViewHolderProvider[_ <: A] =
    providers find (_.layoutId == viewType) getOrElse {
      throw new IllegalArgumentException(s"unknown viewType: $viewType")
    }
}
