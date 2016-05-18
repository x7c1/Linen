package x7c1.wheat.modern.database.selector.presets

import android.database.{Cursor, SQLException}
import x7c1.wheat.modern.database.selector.{CanIdentify, CursorReadable, CursorReifiable}

import scala.language.{higherKinds, reflectiveCalls}

abstract class CanTraverseRecord[
  I[T] <: CanIdentify[T],
  A: CursorReifiable: ({ type L[T] = CursorReadable[A, T] })#L
] extends CanTraverse[I, A]{

  override def fromCursor(cursor: Cursor): Either[SQLException, ClosableSequence[A]] = {
    val sequence = ClosableSequence[A, A](cursor)
    Right(sequence)
  }

}
