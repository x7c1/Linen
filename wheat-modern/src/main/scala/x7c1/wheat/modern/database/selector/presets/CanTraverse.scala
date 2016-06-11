package x7c1.wheat.modern.database.selector.presets

import android.database.{Cursor, SQLException}
import x7c1.wheat.modern.database.selector.{CanExtract, CanIdentify, CanSelect, CursorReadable, CursorReifiable}
import x7c1.wheat.modern.sequence.{CanFilterFrom, CanMapFrom, Sequence}

import scala.language.{higherKinds, reflectiveCalls}

trait CanTraverse[I[T] <: CanIdentify[T], A] extends CanExtract[I, ClosableSequence[A]]{
  override type Result[X] = Either[SQLException, X]
}

trait CanTraverseBySelect [I[T] <: CanIdentify[T], A]
  extends CanTraverse[I, A] with CanSelect[I, ClosableSequence[A]]{

  override def onException(e: SQLException) = {
    Left(e)
  }
  override def atFinal(cursor: Cursor): Unit = {
    // nop
  }
}

trait ClosableSequence[+A] extends Sequence[A]{
  def closeCursor(): Unit
}

object ClosableSequence {
  def apply[
    FROM: CursorReifiable,
    TO: ({ type L[T] = CursorReadable[FROM, T] })#L](cursor: Cursor): ClosableSequence[TO] = {

    new ClosableSequence[TO] {
      lazy val read = implicitly[CursorReadable[FROM, TO]].readAt
      lazy val typed = implicitly[CursorReifiable[FROM]].reify(cursor)
      override def closeCursor() = cursor.close()
      override def findAt(position: Int) = read(typed, position)
      override def length = cursor.getCount
    }
  }
  implicit object canMapFrom extends CanMapFrom[ClosableSequence]{
    override def mapFrom[A, B](fa: ClosableSequence[A])(f: A => B): ClosableSequence[B] =
      new ClosableSequence[B] {
        override def closeCursor() = fa.closeCursor()
        override def findAt(position: Int) = fa.findAt(position) map f
        override def length = fa.length
      }
  }
  implicit object canFilterFrom extends CanFilterFrom[ClosableSequence]{
    override def asFiltered[A](fa: ClosableSequence[A])(filtered: Map[Int, Int]) =
      new ClosableSequence[A] {
        override def closeCursor() = fa.closeCursor()
        override def findAt(position: Int) = filtered get position flatMap fa.findAt
        override def length = filtered.size
      }
  }
}
