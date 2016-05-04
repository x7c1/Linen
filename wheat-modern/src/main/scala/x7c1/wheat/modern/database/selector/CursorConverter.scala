package x7c1.wheat.modern.database.selector

import android.database.Cursor

import scala.language.reflectiveCalls

class CursorConverter[
  A: CursorReifiable,
  X: ({ type L[T] = CursorConvertible[A, T] })#L
](cursor: Cursor) {

  private lazy val typed = implicitly[CursorReifiable[A]].reify(cursor)

  private val fromCursor = implicitly[CursorConvertible[A, X]].fromCursor

  def convertAt(position: Int): Option[X] = {
    fromCursor(typed, position)
  }
}
