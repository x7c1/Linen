package x7c1.wheat.modern.database.selector.presets

import android.database.{Cursor, SQLException}
import x7c1.wheat.modern.database.selector.{CanExtract, CanIdentify, CanSelect}

import scala.language.higherKinds

trait CanDetect[I[T] <: CanIdentify[T], A] extends CanExtract[I, Boolean]{
  override type Result[X] = Either[SQLException, X]
}

trait CanDetectBySelect[I[T] <: CanIdentify[T], A]
  extends CanDetect[I, A] with CanSelect[I, Boolean]{

  override def fromCursor(cursor: Cursor) = {
    Right(reify(cursor))
  }
  override def onException(e: SQLException) = {
    Left(e)
  }
  override def atFinal(cursor: Cursor): Unit = {
    cursor.close()
  }
  def reify(cursor: Cursor): Boolean
}
