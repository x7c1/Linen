package x7c1.linen.repository.entry.unread

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer

trait EntryRowsBinder[A <: UnreadEntry] extends ClosableEntryAccessor[A]{
  def append(sequence: ClosableEntryAccessor[A]): Unit
}

private class EntryRowsBinderImpl[A <: UnreadEntry] (
  accessors: ListBuffer[ClosableEntryAccessor[A]]) extends EntryRowsBinder[A]{

  def append(sequence: ClosableEntryAccessor[A]): Unit = {
    accessors += sequence
  }
  override def findAt(position: Int) = {
    findAccessor(accessors, position, 0) flatMap { case (accessor, prev) =>
      accessor.findAt(position - prev)
    }
  }

  override def length: Int = {
    accessors.foldLeft(0){ _ + _.length }
  }

  override def firstEntryPositionOf(sourceId: Long): Option[Int] = {
    @tailrec
    def loop(accessors: Seq[EntryAccessor[A]], prev: Int): Option[Int] =
      accessors match {
        case x +: xs => x.firstEntryPositionOf(sourceId) match {
          case Some(s) => Some(prev + s)
          case None => loop(xs, x.length + prev)
        }
        case Seq() => None
      }

    loop(accessors, 0)
  }

  override def findKindAt(position: Int) = {
    findAccessor(accessors, position, 0) flatMap { case (accessor, prev) =>
      accessor.findKindAt(position - prev)
    }
  }

  @tailrec
  private def findAccessor(
    accessors: Seq[EntryAccessor[A]],
    position: Int,
    prev: Int): Option[(EntryAccessor[A], Int)] = {

    accessors match {
      case x +: xs => x.length + prev match {
        case sum if sum > position => Some(x -> prev)
        case sum => findAccessor(xs, position, sum)
      }
      case Seq() => None
    }
  }

  override def close(): Unit = synchronized {
    accessors foreach (_.close())
    accessors.clear()
  }
  override def lastEntriesTo(position: Int) = {
    @tailrec
    def loop(
      accessors: Seq[EntryAccessor[A]],
      remains: Int, entries: Seq[A]): Seq[A] = {

      accessors match {
        case head +: tail =>
          val xs = entries ++ head.lastEntriesTo(remains)
          val diff = remains - head.length
          if (diff <= 0){
            xs
          } else {
            loop(tail, diff, xs)
          }
        case Seq() => entries
      }
    }
    loop(accessors, position, Seq())
  }

  override def latestEntriesTo(position: Int): Seq[A] = {
    @tailrec
    def loop(
      accessors: Seq[EntryAccessor[A]],
      remains: Int, entries: Seq[A]): Seq[A] = {

      accessors match {
        case head +: tail =>
          val xs = entries ++ head.latestEntriesTo(remains)
          val diff = remains - head.length
          if (diff <= 0){
            xs
          } else {
            loop(tail, diff, xs)
          }
        case Seq() => entries
      }
    }
    loop(accessors, position, Seq())
  }
}

object EntryRowsBinder {
  def apply[A <: UnreadEntry](): EntryRowsBinder[A] = {
    new EntryRowsBinderImpl[A](new ListBuffer())
  }
}
