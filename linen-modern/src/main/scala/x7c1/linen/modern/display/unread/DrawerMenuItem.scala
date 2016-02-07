package x7c1.linen.modern.display.unread

import android.view.{ViewGroup, View}
import android.view.View.OnClickListener
import x7c1.wheat.ancient.resource.ViewHolderProvider

trait DrawerMenuItem[+A] {
  def viewType: Int = viewHolderProvider.layoutId()
  def viewHolderProvider: ViewHolderProvider[_ <: A]
}

trait MenuTitle {
  def body: String
}

trait TypedMenuTitle[+A] extends DrawerMenuItem[A] with MenuTitle

object TypedMenuTitle {
  def apply[A](body: String, provider: ViewHolderProvider[A]): TypedMenuTitle[A] = {
    new TypedMenuTitleImpl(body, provider)
  }
  private class TypedMenuTitleImpl[A](
    override val body: String,
    override val viewHolderProvider: ViewHolderProvider[A] ) extends TypedMenuTitle[A]
}

trait MenuLabel {
  def body: String
  def onClick: OnClickListener
}

trait TypedMenuLabel[+A] extends DrawerMenuItem[A] with MenuLabel

object TypedMenuLabel {
  def apply[A](
    body: String,
    provider: ViewHolderProvider[A],
    listener: OnClickListener ): TypedMenuLabel[A] = {

    new TypedMenuLabelImpl(body, provider, listener)
  }
  private class TypedMenuLabelImpl[A](
    override val body: String,
    override val viewHolderProvider: ViewHolderProvider[A],
    override val onClick: OnClickListener ) extends TypedMenuLabel[A]
}
class DrawerMenuLabelFactory[A](
  listener: OnDrawerMenuClickListener, provider: ViewHolderProvider[A]){

  def of(kind: DrawerMenuItemKind): TypedMenuLabel[A] = {
    TypedMenuLabel(kind.body, provider, new OnClickListener {
      override def onClick(v: View): Unit = listener onClick kind
    })
  }
}
class DrawerMenuTitleFactory[A](
  provider: ViewHolderProvider[A]){

  def of(body: String): TypedMenuTitle[A] = {
    TypedMenuTitle(body, provider)
  }
}

trait MenuItemsContainer[+A] {
  def length: Int
  def itemAt(position: Int): DrawerMenuItem[A]
  def viewHolderProviders: Seq[ViewHolderProvider[_ <: A]]
}

class MenuItemSeparator[A](
  viewHolderProvider0: ViewHolderProvider[A]) extends MenuItemsContainer[A] {

  private lazy val separator = new DrawerMenuItem[A] {
    override def viewHolderProvider = viewHolderProvider0
  }
  override def length: Int = 1

  override def itemAt(position: Int): DrawerMenuItem[A] = separator

  override lazy val viewHolderProviders = Seq(viewHolderProvider0)
}

class MenuItemsBox[A](label: TypedMenuTitle[A], items: TypedMenuLabel[A]*) extends MenuItemsContainer[A] {

  override def length: Int = 1 + items.size

  override def itemAt(position: Int): DrawerMenuItem[A] = position match {
    case 0 => label
    case n => items(n - 1)
  }
  override def viewHolderProviders = {
    label.viewHolderProvider +: items.map(_.viewHolderProvider)
  }
}

case class MenuItemsBoxes[A](containers: MenuItemsContainer[A]*){
  import scala.annotation.tailrec
  require(containers.nonEmpty, "empty boxes")

  def count: Int = containers.foldLeft(0){_ + _.length}

  def findItemAt(position: Int): Option[DrawerMenuItem[A]] = {
    @tailrec
    def loop(
      boxes: Seq[MenuItemsContainer[A]],
      prev: Int): Option[(MenuItemsContainer[A], Int)] = {

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
  def inflateOn(parent: ViewGroup, viewType: Int): A = {
    providers get viewType inflateOn parent
  }
  private def providers: ViewHolderProviders[A] = {
    val xs = containers.flatMap(_.viewHolderProviders)
    new ViewHolderProviders(xs:_*)
  }
}

trait OnDrawerMenuClickListener {
  def onClick(kind: DrawerMenuItemKind): Unit
}

private class ViewHolderProviders[A](providers: ViewHolderProvider[_ <: A]*){
  def get(viewType: Int): ViewHolderProvider[_ <: A] =
    providers find (_.layoutId == viewType) getOrElse {
      throw new IllegalArgumentException(s"unknown viewType: $viewType")
    }
}
