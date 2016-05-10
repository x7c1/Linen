package x7c1.wheat.modern.database.selector.presets

import android.database.{Cursor, SQLException}
import x7c1.wheat.modern.database.selector.{CanIdentify, CanSelect, CursorReadable, CursorReifiable}
import x7c1.wheat.modern.sequence.Sequence

import scala.language.{reflectiveCalls, higherKinds}

abstract class CanTraverse [I[T] <: CanIdentify[T], A]
  extends CanSelect[I, ClosableSequence[A]]{

  override type Result[X] = Either[SQLException, X]

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
}
