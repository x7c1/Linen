package x7c1.wheat.modern.database.selector

import android.database.SQLException
import android.database.sqlite.SQLiteDatabase

import scala.language.higherKinds

class RecordSelector[A](val db: SQLiteDatabase) extends AnyVal {

  def selectBy[X: I, I[T] <: CanIdentify[T]](id: X)(implicit i: CanSelect[I, A]): i.Result[A] = {
    try {
      val query = i query id
      val cursor = db.rawQuery(query.sql, query.selectionArgs)
      try i fromCursor cursor
      finally i atFinal cursor
    } catch {
      case e: SQLException => i onException e
    }
  }
}

object RecordSelector {
  def apply[A](db: SQLiteDatabase): RecordSelector[A] = {
    new RecordSelector[A](db)
  }
}