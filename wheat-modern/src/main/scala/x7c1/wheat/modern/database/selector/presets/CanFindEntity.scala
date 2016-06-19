package x7c1.wheat.modern.database.selector.presets

import android.database.Cursor
import x7c1.wheat.macros.database.Query
import x7c1.wheat.modern.database.selector.{CanIdentify, CursorConverter, CursorReadable, CursorReifiable}

import scala.language.{higherKinds, reflectiveCalls}

abstract class CanFindEntity[
  I[T] <: CanIdentify[T],
  FROM: CursorReifiable: ({ type L[T] = CanFindBySelect[I, T] })#L,
  TO: ({ type L[T] = CursorReadable[FROM, T] })#L
] extends CanFindBySelect[I, TO]{

  override def reify(cursor: Cursor): Option[TO] = {
    new CursorConverter[FROM, TO](cursor) convertAt 0
  }
  override def queryAbout[X: I](target: X): Query = {
    implicitly[CanFindBySelect[I, FROM]] queryAbout target
  }
}
