package x7c1.wheat.modern.database.selector

import android.database.SQLException
import android.database.sqlite.SQLiteDatabase

import scala.language.higherKinds

class ItemSelector[A](val db: SQLiteDatabase) extends AnyVal {

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
  def select(implicit i: CanSelectDirectly[A]): i.Result[A] = {
    try {
      val query = i.query
      val cursor = db.rawQuery(query.sql, query.selectionArgs)
      try i fromCursor cursor
      finally i atFinal cursor
    } catch {
      case e: SQLException => i onException e
    }
  }
}

object ItemSelector {
  def apply[A](db: SQLiteDatabase): ItemSelector[A] = {
    new ItemSelector[A](db)
  }
}