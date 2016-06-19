package x7c1.wheat.modern.database.selector.presets

import android.database.{Cursor, SQLException}
import x7c1.wheat.modern.database.Query
import x7c1.wheat.modern.database.selector.{CanSelect, CursorReadable, CursorReifiable, UnitIdentifiable}

import scala.language.reflectiveCalls

trait CanTraverseByQuery[A] extends CanSelect[UnitIdentifiable, ClosableSequence[A]]{
  override type Result[X] = Either[SQLException, X]

  override def fromCursor(cursor: Cursor) = {
    Right(reify(cursor))
  }
  override def onException(e: SQLException) = {
    Left(e)
  }
  override def atFinal(cursor: Cursor): Unit = {
    //nop
  }
  def reify(cursor: Cursor): ClosableSequence[A]
}

abstract class CanTraverseEntityByQuery[
  FROM: CursorReifiable,
  TO: ({ type L[T] = CursorReadable[FROM, T] })#L
](query: Query) extends CanTraverseByQuery[TO]{

  override def reify(cursor: Cursor) = {
    ClosableSequence[FROM, TO](cursor)
  }
  override def queryAbout[X: UnitIdentifiable](target: X) = query
}

abstract class CanTraverseRecordByQuery[
  A: CursorReifiable: ({ type L[T] = CursorReadable[A, T] })#L
](query: Query) extends CanTraverseByQuery[A]{

  override def reify(cursor: Cursor) = {
    ClosableSequence[A, A](cursor)
  }
  override def queryAbout[X: UnitIdentifiable](target: X) = query
}
