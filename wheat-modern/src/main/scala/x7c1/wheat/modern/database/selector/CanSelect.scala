package x7c1.wheat.modern.database.selector

import android.database.{Cursor, SQLException}
import x7c1.wheat.macros.database.TypedCursor
import x7c1.wheat.modern.database.Query

import scala.language.{higherKinds, reflectiveCalls}

trait CanSelect[I[T] <: CanIdentify[T], A]{
  type Result[_]
  def query[X: I](target: X): Query
  def fromCursor(cursor: Cursor): Result[A]
  def onException(e: SQLException): Result[A]
  def atFinal(cursor: Cursor): Unit
}

trait CursorReadable[FROM, TO]{
  def readAt: (FROM with TypedCursor[FROM], Int) => Option[TO]
}

trait CursorConvertible[FROM, TO] extends CursorReadable[FROM, TO]{
  override def readAt = {
    case (cursor, position) => cursor.moveToFind(position){
      fromCursor(cursor)
    }
  }
  def fromCursor: FROM with TypedCursor[FROM] => TO
}

trait CursorReifiable[A] {
  def reify(cursor: Cursor): A with TypedCursor[A]
}

trait RecordReifiable[A] extends CursorReifiable[A] with CursorReadable[A, A]{
  override def readAt = _ freezeAt _
}
