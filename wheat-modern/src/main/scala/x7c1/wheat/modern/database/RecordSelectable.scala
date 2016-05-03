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

abstract class RecordFindable[
  I[T] <: RecordIdentifiable[T],
  A: CursorReifiable: ({ type L[T] = CursorConvertible[A, T] })#L
] extends RawFindable[I, A]{

  override def reify(cursor: Cursor): Option[A] = {
    val typed = implicitly[CursorReifiable[A]].reify(cursor)
    implicitly[CursorConvertible[A, A]].fromCursor(typed, 0)
  }
}

object RecordFindable {
  abstract class Where[
    I[T] <: RecordIdentifiable[T],
    A: CursorReifiable: ({ type L[T] = CursorConvertible[A, T] })#L
  ](table: String) extends RecordFindable[I, A]{

    override def query[X: I](target: X): Query = {
      val id = implicitly[I[X]] idOf target
      val clause = where(id) map { case (key, _) => s"$key = ?" } mkString " AND "
      val sql = s"SELECT * FROM $table WHERE $clause"
      val args = where(id) map { case (_, value) => value }
      new Query(sql, args.toArray)
    }
    def where[X](id: I[X]#ID): Seq[(String, String)]
  }
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