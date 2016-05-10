package x7c1.wheat.modern.database.selector.presets

import android.database.{Cursor, SQLException}
import x7c1.wheat.modern.database.selector.{CanSelect, CanIdentify}
import x7c1.wheat.modern.sequence.Sequence

import scala.language.higherKinds

abstract class CanTraverse [I[T] <: CanIdentify[T], A]
  extends CanSelect[I, CursorClosableSequence[A]]{

  override type Result[X] = Either[SQLException, X]

  override def onException(e: SQLException) = {
    Left(e)
  }
  override def atFinal(cursor: Cursor): Unit = {
    // nop
  }
}

trait CursorClosableSequence[+A] extends Sequence[A]{
  def closeCursor(): Unit
}
