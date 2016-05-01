package x7c1.wheat.modern.database

import android.database.{Cursor, SQLException}
import x7c1.wheat.macros.database.TypedCursor
import x7c1.wheat.modern.either.{OptionRight, OptionLeft, OptionEither}

import scala.language.higherKinds

trait MultipleSelectable [A, ID]{
  type Result[_]
  def query(id: ID): Query
  def fromCursor(cursor: Cursor): Result[A]
  def onException(e: SQLException): Result[A]
  def atFinal(cursor: Cursor): Unit
}

trait SeqSelectable[A, ID] extends MultipleSelectable[Seq[A], ID]{

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

trait Findable[A, ID] extends MultipleSelectable[A, ID]{

  override type Result[X] = OptionEither[SQLException, X]

  override def fromCursor(cursor: Cursor) = {
    OptionRight(reify(cursor))
  }
  override def onException(e: SQLException) = {
    OptionLeft(e)
  }
  override def atFinal(cursor: Cursor) = {
    cursor.close()
  }
  def reify(cursor: Cursor): Option[A]
}

trait MultipleSelectable2[I[T] <: RecordIdentifiable[T], A]{
  type Result[_]
  def query[X: I](target: X): Query
  def fromCursor(cursor: Cursor): Result[A]
  def onException(e: SQLException): Result[A]
  def atFinal(cursor: Cursor): Unit
}

trait Findable2[I[T] <: RecordIdentifiable[T], A]
  extends MultipleSelectable2[I, Option[A]]{

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

trait SeqSelectable2[I[T] <: RecordIdentifiable[T], A]
  extends MultipleSelectable2[I, Seq[A]]{

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
