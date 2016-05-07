package x7c1.wheat.modern.database.selector.presets

import android.database.{Cursor, SQLException}
import x7c1.wheat.modern.database.selector.{CanIdentify, CanSelect}

import scala.language.higherKinds

abstract class CanCollect[I[T] <: CanIdentify[T], A]
  extends CanSelect[I, Seq[A]]{

  override type Result[X] = Either[SQLException, X]

  override def onException(e: SQLException) = {
    Left(e)
  }
  override def atFinal(cursor: Cursor) = {
    cursor.close()
  }
}
