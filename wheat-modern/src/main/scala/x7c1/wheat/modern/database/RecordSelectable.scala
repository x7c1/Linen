package x7c1.wheat.modern.database

import android.database.{Cursor, SQLException}
import x7c1.wheat.macros.database.TypedCursor

import scala.language.higherKinds

trait RecordSelectable[I[T] <: RecordIdentifiable[T], A]{
  type Result[_]
  def query[X: I](target: X): Query
  def fromCursor(cursor: Cursor): Result[A]
  def onException(e: SQLException): Result[A]
  def atFinal(cursor: Cursor): Unit
}

trait RecordFindable[I[T] <: RecordIdentifiable[T], A]
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

trait SeqSelectable[I[T] <: RecordIdentifiable[T], A]
  extends RecordSelectable[I, Seq[A]]{

  override type Result[X] = Either[SQLException, X]

  override def fromCursor(cursor: Cursor) = {
    val typed = reify(cursor)
    val xs = (0 to cursor.getColumnCount - 1) flatMap { typed.freezeAt }
    Right(xs)
  }
  override def onException(e: SQLException) = {
    Left(e)
  }
  override def atFinal(cursor: Cursor) = {
    cursor.close()
  }
  def reify(cursor: Cursor): TypedCursor[A]
}
