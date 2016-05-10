package x7c1.wheat.modern.database.selector.presets

import android.database.{SQLException, Cursor}
import x7c1.wheat.modern.database.Query
import x7c1.wheat.modern.database.selector.{CursorReadable, CursorReifiable, CanIdentify}

import scala.language.{reflectiveCalls, higherKinds}

abstract class CanTraverseEntity[
  I[T] <: CanIdentify[T],
  FROM: CursorReifiable: ({ type L[T] = CanTraverseRecord[I, T] })#L,
  TO: ({ type L[T] = CursorReadable[FROM, T] })#L
] extends CanTraverse[I, TO]{

  override def query[X: I](target: X): Query = {
    implicitly[CanTraverseRecord[I, FROM]] query target
  }
  override def fromCursor(cursor: Cursor): Either[SQLException, ClosableSequence[TO]] = {
    val sequence = ClosableSequence[FROM, TO](cursor)
    Right(sequence)
  }
}
