package x7c1.linen.modern.display.unread

import android.view.View
import android.view.View.OnClickListener
import x7c1.wheat.ancient.resource.ViewHolderProvider

trait DrawerMenuItem {
  def viewType: Int
}

trait DrawerMenuTitle extends DrawerMenuItem {
  def body: String
  def viewType: Int
}
object DrawerMenuTitle {
  def apply(body: String, provider: ViewHolderProvider[_]): DrawerMenuTitle = {
    new DrawerMenuTitleImpl(body, provider.layoutId)
  }
  private class DrawerMenuTitleImpl(
    override val body: String,
    override val viewType: Int ) extends DrawerMenuTitle
}

trait DrawerMenuLabel extends DrawerMenuItem {
  def body: String
  def viewType: Int
  def onClick: OnClickListener
}
object DrawerMenuLabel {
  def apply(
    body: String,
    provider: ViewHolderProvider[_],
    listener: OnClickListener ): DrawerMenuLabel = {

    new DrawerMenuLabelImpl(body, provider.layoutId, listener)
  }
  private class DrawerMenuLabelImpl(
    override val body: String,
    override val viewType: Int,
    override val onClick: OnClickListener ) extends DrawerMenuLabel
}
class DrawerMenuLabelFactory(
  listener: OnDrawerMenuClickListener, provider: ViewHolderProvider[_]){

  def of(kind: DrawerMenuItemKind): DrawerMenuLabel = {
    DrawerMenuLabel(kind.body, provider, new OnClickListener {
      override def onClick(v: View): Unit = listener onClick kind
    })
  }
}
class DrawerMenuTitleFactory(
  provider: ViewHolderProvider[_]){

  def of(body: String): DrawerMenuTitle = {
    DrawerMenuTitle(body, provider)
  }
}

trait MenuItemsContainer {
  def length: Int
  def itemAt(position: Int): DrawerMenuItem
}

class MenuItemSeparator(
  viewHolderProvider: ViewHolderProvider[_]) extends MenuItemsContainer {

  private lazy val separator = new DrawerMenuItem {
    override def viewType: Int = viewHolderProvider.layoutId()
  }
  override def length: Int = 1

  override def itemAt(position: Int): DrawerMenuItem = separator
}

class MenuItemsBox(label: DrawerMenuTitle, items: DrawerMenuLabel*) extends MenuItemsContainer {

  override def length: Int = 1 + items.size

  override def itemAt(position: Int): DrawerMenuItem = position match {
    case 0 => label
    case n => items(n - 1)
  }
}

case class MenuItemsBoxes(containers: MenuItemsContainer*){
  import scala.annotation.tailrec
  require(containers.nonEmpty, "empty boxes")

  def count: Int = containers.foldLeft(0){_ + _.length}

  def findItemAt(position: Int): Option[DrawerMenuItem] = {
    @tailrec
    def loop(boxes: Seq[MenuItemsContainer], prev: Int): Option[(MenuItemsContainer, Int)] = {
      boxes match {
        case x +: xs => x.length + prev match {
          case sum if sum > position => Some(x -> prev)
          case sum => loop(xs, sum)
        }
        case Seq() => None
      }
    }
    loop(containers, 0) map { case (x, prev) => x.itemAt(position - prev) }
  }
}

trait OnDrawerMenuClickListener {
  def onClick(kind: DrawerMenuItemKind): Unit
}
