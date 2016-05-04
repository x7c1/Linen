package x7c1.wheat.modern.database.selector.presets

import android.database.Cursor
import x7c1.wheat.modern.database.selector.{CursorReifiable, CursorConvertible, CursorConverter, CanIdentify}
import x7c1.wheat.modern.database.Query

import scala.language.higherKinds

abstract class CanFindEntity[
  I[T] <: CanIdentify[T],
  FROM: CursorReifiable: ({ type L[T] = CanFindRecord[I, T] })#L,
  TO: ({ type L[T] = CursorConvertible[FROM, T] })#L
] extends CanFind[I, TO]{

  override def reify(cursor: Cursor): Option[TO] = {
    new CursorConverter[FROM, TO](cursor) convertAt 0
  }
  override def query[X: I](target: X): Query = {
    implicitly[CanFindRecord[I, FROM]] query target
  }
}
