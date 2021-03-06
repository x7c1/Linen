package x7c1.wheat.modern.database.selector.presets

import android.database.{Cursor, SQLException}
import x7c1.wheat.modern.database.selector.{CanExtract, CanIdentify, CanSelect, CursorReadable, CursorReifiable}
import x7c1.wheat.modern.sequence.{CanDelegate, CanFilterFrom, CanMapFrom, CanSliceFrom, Sequence}

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
    override def asFiltered[A](fa: ClosableSequence[A])(positions: Sequence[Int]) =
      new ClosableSequence[A] {
        override def closeCursor() = fa.closeCursor()
        override def findAt(position: Int) = positions findAt position flatMap fa.findAt
        override def length = positions.length
      }
  }
  implicit object canSliceFrom extends CanSliceFrom[ClosableSequence]{
    override def sliceFrom[A](fa: ClosableSequence[A])(range: Seq[Int]) =
      new ClosableSequence[A] {
        lazy val positionAt = range.lift
        override def closeCursor() = fa.closeCursor()
        override def findAt(position: Int) = positionAt(position) flatMap fa.findAt
        override def length = range.length
      }
  }
  implicit object canDelegate extends CanDelegate[ClosableSequence]{
    override def delegate[A, B](from: ClosableSequence[A])(to: Sequence[B]) =
      new ClosableSequence[B] {
        override def closeCursor() = from.closeCursor()
        override def findAt(position: Int) = to findAt position
        override def length = to.length
      }
  }
}
