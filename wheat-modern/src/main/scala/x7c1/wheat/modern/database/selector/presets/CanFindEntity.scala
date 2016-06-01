package x7c1.wheat.modern.database.selector.presets

import android.database.Cursor
import x7c1.wheat.modern.database.selector.{CursorReifiable, CursorReadable, CursorConverter, CanIdentify}
import x7c1.wheat.modern.database.Query

import scala.language.{reflectiveCalls, higherKinds}

abstract class CanFindEntity[
  I[T] <: CanIdentify[T],
  FROM: CursorReifiable: ({ type L[T] = CanFindBySelect[I, T] })#L,
  TO: ({ type L[T] = CursorReadable[FROM, T] })#L
] extends CanFindBySelect[I, TO]{

  override def reify(cursor: Cursor): Option[TO] = {
    new CursorConverter[FROM, TO](cursor) convertAt 0
  }
  override def query[X: I](target: X): Query = {
    implicitly[CanFindBySelect[I, FROM]] query target
  }
}
