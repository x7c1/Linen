package x7c1.wheat.modern.database.selector.presets

import android.database.Cursor
import x7c1.wheat.modern.database.{HasTable, Query}
import x7c1.wheat.modern.database.selector.{CanIdentify, CursorConverter, CursorReadable, CursorReifiable}

import scala.language.{higherKinds, reflectiveCalls}

abstract class CanCollectRecord[
  I[T] <: CanIdentify[T],
  A: CursorReifiable: ({ type L[T] = CursorReadable[A, T] })#L
] extends CanCollectBySelect[I, A]{

  override def fromCursor(cursor: Cursor) = {
    val converter = new CursorConverter[A, A](cursor)
    val xs = 0 until cursor.getCount flatMap { converter.convertAt }
    Right(xs)
  }
}

object CanCollectRecord {
  abstract class Where[
    I[T] <: CanIdentify[T],
    A: HasTable: CursorReifiable: ({ type L[T] = CursorReadable[A, T] })#L
  ] extends CanCollectRecord[I, A]{

    override def query[X: I](target: X): Query = {
      val table = implicitly[HasTable[A]].tableName
      QueryFactory[I](table).create(target)(where)
    }
    def where[X](id: I[X]#ID): Seq[(String, String)]
  }
}
