package x7c1.wheat.modern.database

import android.database.{Cursor, SQLException}
import x7c1.wheat.macros.database.TypedCursor

import scala.language.{reflectiveCalls, higherKinds}

trait RecordSelectable[I[T] <: RecordIdentifiable[T], A]{
  type Result[_]
  def query[X: I](target: X): Query
  def fromCursor(cursor: Cursor): Result[A]
  def onException(e: SQLException): Result[A]
  def atFinal(cursor: Cursor): Unit
}

abstract class RawFindable[I[T] <: RecordIdentifiable[T], A]
  extends RecordSelectable[I, Option[A]]{

  override type Result[X] = Either[SQLException, X]

  override def fromCursor(cursor: Cursor) = {
    Right(reify(cursor))
  }
  override def onException(e: SQLException) = {
    Left(e)
  }
  override def atFinal(cursor: Cursor) = {
    cursor.close()
  }
  def reify(cursor: Cursor): Option[A]
}

abstract class SeqSelectable[I[T] <: RecordIdentifiable[T], A: CursorReifiable]
  extends RecordSelectable[I, Seq[A]]{

  override type Result[X] = Either[SQLException, X]

  override def fromCursor(cursor: Cursor) = {
    val typed = implicitly[CursorReifiable[A]].reify(cursor)
    val xs = (0 to cursor.getColumnCount - 1) flatMap { typed.freezeAt }
    Right(xs)
  }
  override def onException(e: SQLException) = {
    Left(e)
  }
  override def atFinal(cursor: Cursor) = {
    cursor.close()
  }
}

class CursorConverter[
  A: CursorReifiable,
  X: ({ type L[T] = CursorConvertible[A, T] })#L](cursor: Cursor){

  private lazy val typed = implicitly[CursorReifiable[A]].reify(cursor)

  def convertAt(position: Int): Option[X] = {
    implicitly[CursorConvertible[A, X]].fromCursor(typed, position)
  }
}

trait CursorConvertible[FROM, A]{
  def fromCursor: (FROM with TypedCursor[FROM], Int) => Option[A]
}
trait CursorReifiable[A] {
  def reify(cursor: Cursor): A with TypedCursor[A]
}

trait RecordReifiable[A] extends CursorReifiable[A] with CursorConvertible[A, A]{
  override def fromCursor = _ freezeAt _
}