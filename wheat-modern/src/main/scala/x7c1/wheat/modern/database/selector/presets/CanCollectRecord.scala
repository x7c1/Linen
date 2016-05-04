package x7c1.wheat.modern.database.selector.presets

import android.database.Cursor
import x7c1.wheat.modern.database.selector.{CanIdentify, CursorReifiable}

import scala.language.higherKinds

abstract class CanCollectRecord[I[T] <: CanIdentify[T], A: CursorReifiable]
  extends CanCollect[I, A]{

  override def fromCursor(cursor: Cursor) = {
    val typed = implicitly[CursorReifiable[A]].reify(cursor)
    val xs = (0 to cursor.getColumnCount - 1) flatMap { typed.freezeAt }
    Right(xs)
  }
}
