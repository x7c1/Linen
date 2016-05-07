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
      QueryFactory[I](table).create(target)(where)
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
