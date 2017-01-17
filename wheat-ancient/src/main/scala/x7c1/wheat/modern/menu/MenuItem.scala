package x7c1.wheat.modern.menu

import android.view.ViewGroup
import x7c1.wheat.ancient.resource.ViewHolderProvider

import scala.annotation.tailrec

trait MenuItem[+A] {
  def length: Int
  def findItemAt(position: Int): Option[SingleMenuItem[A]]
  def viewHolderProviders[B >: A]: Seq[ViewHolderProvider[_ <: B]]
}

trait MenuText {
  def text: String
}

class SingleMenuItem[+A](
  provider: ViewHolderProvider[A]) extends MenuItem[A] {

  override val length: Int = 1

  override def findItemAt(position: Int) = Some(this)

  override def viewHolderProviders[B >: A]: Seq[ViewHolderProvider[_ <: B]] = Seq(provider)

  def viewType: Int = provider.layoutId()
}

class SingleMenuText[A](
  override val text: String,
  provider: ViewHolderProvider[A]) extends SingleMenuItem(provider) with MenuText

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
  override def viewHolderProviders[B >: A] = {
    items.flatMap(_.viewHolderProviders)
  }
  def inflate(parent: ViewGroup, viewType: Int): A = {
    providers get viewType inflateOn parent
  }
  def bind[B <: A]
    (holder: B, position: Int)
    (block: ((B, SingleMenuItem[A])) => Unit) = {

    findItemAt(position) map (holder -> _) foreach block
  }
  def viewTypeAt(position: Int): Int = {
    findItemAt(position) map (_.viewType) getOrElse {
      throw new IllegalArgumentException(s"invalid position: $position")
    }
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