package x7c1.wheat.modern.database

import android.database.Cursor

import scala.language.{reflectiveCalls, higherKinds}

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

abstract class RecordFindable[
  I[T] <: RecordIdentifiable[T],
  A: CursorReifiable: ({ type L[T] = CursorConvertible[A, T] })#L
] extends RawFindable[I, A]{

  override def reify(cursor: Cursor): Option[A] = {
    new CursorConverter[A, A](cursor) convertAt 0
  }
}
