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

trait CanSelectDirectly[A]{
  type Result[_]
  def query: Query
  def fromCursor(cursor: Cursor): Result[A]
  def onException(e: SQLException): Result[A]
  def atFinal(cursor: Cursor): Unit
}

trait CursorConvertible[FROM, TO]{
  def fromCursor: (FROM with TypedCursor[FROM], Int) => Option[TO]
}
trait CursorReifiable[A] {
  def reify(cursor: Cursor): A with TypedCursor[A]
}

trait RecordReifiable[A] extends CursorReifiable[A] with CursorConvertible[A, A]{
  override def fromCursor = _ freezeAt _
}
