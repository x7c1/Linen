package x7c1.wheat.modern.database.selector.presets

import android.database.Cursor
import x7c1.wheat.macros.database.Query
import x7c1.wheat.modern.database.selector.{CanIdentify, CursorConverter, CursorReadable, CursorReifiable}
import x7c1.wheat.modern.database.HasTable

import scala.language.{higherKinds, reflectiveCalls}

object CanFindRecord {
  abstract class Where[
    I[T] <: CanIdentify[T],
    A: HasTable: CursorReifiable: ({ type L[T] = CursorReadable[A, T] })#L
  ] extends CanFindRecord[I, A]{

    override def queryAbout[X: I](target: X): Query = {
      val table = implicitly[HasTable[A]].tableName
      QueryFactory[I](table).create(target)(where)
    }
    def where[X](id: I[X]#ID): Seq[(String, String)]
  }
}

abstract class CanFindRecord[
  I[T] <: CanIdentify[T],
  A: CursorReifiable: ({ type L[T] = CursorReadable[A, T] })#L
] extends CanFindBySelect[I, A]{

  override def reify(cursor: Cursor): Option[A] = {
    new CursorConverter[A, A](cursor) convertAt 0
  }
}
