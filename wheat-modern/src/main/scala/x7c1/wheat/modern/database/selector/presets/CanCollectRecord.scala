package x7c1.wheat.modern.database.selector.presets

import android.database.Cursor
import x7c1.wheat.modern.database.Query
import x7c1.wheat.modern.database.selector.{CanIdentify, CursorReadable, CursorReifiable}

import scala.language.{higherKinds, reflectiveCalls}

abstract class CanCollectRecord[I[T] <: CanIdentify[T], A: CursorReifiable]
  extends CanCollect[I, A]{

  override def fromCursor(cursor: Cursor) = {
    val typed = implicitly[CursorReifiable[A]].reify(cursor)
    val xs = (0 to cursor.getColumnCount - 1) flatMap { typed.freezeAt }
    Right(xs)
  }
}

object CanCollectRecord {
  abstract class Where[
    I[T] <: CanIdentify[T],
    A: CursorReifiable: ({ type L[T] = CursorReadable[A, T] })#L
  ](table: String) extends CanCollectRecord[I, A]{

    override def query[X: I](target: X): Query = {
      QueryFactory[I](table).create(target)(where)
    }
    def where[X](id: I[X]#ID): Seq[(String, String)]
  }
}