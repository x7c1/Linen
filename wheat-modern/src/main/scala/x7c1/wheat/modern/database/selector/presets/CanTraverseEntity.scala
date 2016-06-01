package x7c1.wheat.modern.database.selector.presets

import android.database.{Cursor, SQLException}
import x7c1.wheat.modern.database.Query
import x7c1.wheat.modern.database.selector.{CanIdentify, CursorReadable, CursorReifiable}

import scala.language.{higherKinds, reflectiveCalls}

abstract class CanTraverseEntity[
  I[T] <: CanIdentify[T],
  FROM: CursorReifiable: ({ type L[T] = CanTraverseBySelect[I, T] })#L,
  TO: ({ type L[T] = CursorReadable[FROM, T] })#L
] extends CanTraverseBySelect[I, TO]{

  override def query[X: I](target: X): Query = {
    implicitly[CanTraverseBySelect[I, FROM]] query target
  }
  override def fromCursor(cursor: Cursor): Either[SQLException, ClosableSequence[TO]] = {
    val sequence = ClosableSequence[FROM, TO](cursor)
    Right(sequence)
  }
}
