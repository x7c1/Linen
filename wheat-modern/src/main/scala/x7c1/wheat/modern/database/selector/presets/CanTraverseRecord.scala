package x7c1.wheat.modern.database.selector.presets

import android.database.{Cursor, SQLException}
import x7c1.wheat.modern.database.{HasTable, Query}
import x7c1.wheat.modern.database.selector.{CanIdentify, CursorReadable, CursorReifiable}

import scala.language.{higherKinds, reflectiveCalls}

abstract class CanTraverseRecord[
  I[T] <: CanIdentify[T],
  A: CursorReifiable: ({ type L[T] = CursorReadable[A, T] })#L
] extends CanTraverseBySelect[I, A]{

  override def fromCursor(cursor: Cursor): Either[SQLException, ClosableSequence[A]] = {
    val sequence = ClosableSequence[A, A](cursor)
    Right(sequence)
  }

}

object CanTraverseRecord {
  abstract class Where[
    I[T] <: CanIdentify[T],
    A: HasTable: CursorReifiable: ({ type L[T] = CursorReadable[A, T] })#L
  ] extends CanTraverseRecord[I, A]{

    override def queryAbout[X: I](target: X): Query = {
      val table = implicitly[HasTable[A]].tableName
      QueryFactory[I](table).create(target)(where)
    }
    def where[X](id: I[X]#ID): Seq[(String, String)]
  }
}
