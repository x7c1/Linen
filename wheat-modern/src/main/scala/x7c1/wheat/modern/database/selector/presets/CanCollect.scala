package x7c1.wheat.modern.database.selector.presets

import android.database.{Cursor, SQLException}
import x7c1.wheat.modern.database.selector.{CanExtract, CanIdentify, CanSelect}

import scala.language.higherKinds

trait CanCollect[I[T] <: CanIdentify[T], A] extends CanExtract[I, Seq[A]]{
  override type Result[X] = Either[SQLException, X]
}

trait CanCollectBySelect[I[T] <: CanIdentify[T], A]
  extends CanCollect[I, A] with CanSelect[I, Seq[A]]{

  override def onException(e: SQLException) = {
    Left(e)
  }
  override def atFinal(cursor: Cursor) = {
    cursor.close()
  }
}
