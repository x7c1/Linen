package x7c1.wheat.modern.database.selector.presets

import android.database.{Cursor, SQLException}
import x7c1.wheat.modern.database.selector.{CanIdentify, CanSelect}

import scala.language.higherKinds

abstract class CanFind[I[T] <: CanIdentify[T], A]
  extends CanSelect[I, Option[A]]{

  override type Result[X] = Either[SQLException, X]

  override def fromCursor(cursor: Cursor) = {
    Right(reify(cursor))
  }
  override def onException(e: SQLException) = {
    Left(e)
  }
  override def atFinal(cursor: Cursor) = {
    cursor.close()
  }
  def reify(cursor: Cursor): Option[A]
}
