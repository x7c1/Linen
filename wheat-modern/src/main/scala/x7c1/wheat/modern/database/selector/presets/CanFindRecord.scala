package x7c1.wheat.modern.database.selector.presets

import android.database.Cursor
import x7c1.wheat.modern.database.selector.{CursorReifiable, CursorReadable, CursorConverter, CanIdentify}
import x7c1.wheat.modern.database.Query

import scala.language.{higherKinds, reflectiveCalls}

object CanFindRecord {
  abstract class Where[
    I[T] <: CanIdentify[T],
    A: CursorReifiable: ({ type L[T] = CursorReadable[A, T] })#L
  ](table: String) extends CanFindRecord[I, A]{

    override def query[X: I](target: X): Query = {
      val id = implicitly[I[X]] toId target
      val clause = where(id) map { case (key, _) => s"$key = ?" } mkString " AND "
      val sql = s"SELECT * FROM $table WHERE $clause"
      val args = where(id) map { case (_, value) => value }
      new Query(sql, args.toArray)
    }
    def where[X](id: I[X]#ID): Seq[(String, String)]
  }
}

abstract class CanFindRecord[
  I[T] <: CanIdentify[T],
  A: CursorReifiable: ({ type L[T] = CursorReadable[A, T] })#L
] extends CanFind[I, A]{

  override def reify(cursor: Cursor): Option[A] = {
    new CursorConverter[A, A](cursor) convertAt 0
  }
}
