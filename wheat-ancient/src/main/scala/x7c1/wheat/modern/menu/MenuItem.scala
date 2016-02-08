package x7c1.wheat.modern.menu

import android.view.ViewGroup
import x7c1.wheat.ancient.resource.ViewHolderProvider

import scala.annotation.tailrec

trait MenuItem[+A] {
  def length: Int
  def findItemAt(position: Int): Option[SingleMenuItem[A]]
  def viewHolderProviders: Seq[ViewHolderProvider[_ <: A]]
}

class SingleMenuItem[+A](
  provider: ViewHolderProvider[A]) extends MenuItem[A] {

  override val length: Int = 1

  override def findItemAt(position: Int) = Some(this)

  override def viewHolderProviders: Seq[ViewHolderProvider[_ <: A]] = Seq(provider)

  def viewType: Int = provider.layoutId()
}

class SingleMenuText[A](
  val text: String,
  provider: ViewHolderProvider[A]) extends SingleMenuItem(provider)

class MenuItems[A] private (items: MenuItem[A]*) extends MenuItem[A] {

  override def length: Int = items.foldLeft(0){_ + _.length}

  override def findItemAt(position: Int) = {
    @tailrec
    def loop(items: Seq[MenuItem[A]], prev: Int): Option[(MenuItem[A], Int)] = {
      items match {
        case x +: xs => x.length + prev match {
          case sum if sum > position => Some(x -> prev)
          case sum => loop(xs, sum)
        }
        case Seq() => None
      }
    }
    loop(items, 0) flatMap { case (item, prev) =>
      item.findItemAt(position - prev)
    }
  }
  override def viewHolderProviders = {
    items.flatMap(_.viewHolderProviders)
  }
  def inflateOn(parent: ViewGroup, viewType: Int): A = {
    providers get viewType inflateOn parent
  }
  private def providers: ViewHolderProviders[A] = {
    new ViewHolderProviders(viewHolderProviders:_*)
  }
}

object MenuItems {
  def apply[A](items: MenuItem[A]*): MenuItems[A] = {
    new MenuItems(items:_*)
  }
}

private class ViewHolderProviders[A](providers: ViewHolderProvider[_ <: A]*){
  def get(viewType: Int): ViewHolderProvider[_ <: A] =
    providers find (_.layoutId == viewType) getOrElse {
      throw new IllegalArgumentException(s"unknown viewType: $viewType")
    }
}