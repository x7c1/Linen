package x7c1.wheat.modern.database

import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import x7c1.wheat.modern.either.{OptionEither, OptionLeft, OptionRight}

import scala.language.higherKinds


class ReadableDatabase(db: SQLiteDatabase) {
  def find[A]: SingleSelector[A] = new SingleSelector[A](db)
}

class SingleSelector[A](db: SQLiteDatabase){
  private type QuerySelectable[X] = SingleSelectable[A, X]
  private type ZeroAritySelectable[_] = SingleSelectable[A, Unit]

  def apply[_: ZeroAritySelectable](): OptionEither[SQLException, A] = {
    by({})
  }
  def by[B: QuerySelectable](id: B): OptionEither[SQLException, A] = {
    try {
      val i = implicitly[QuerySelectable[B]]
      val query = i.query(id)
      val cursor = db.rawQuery(query.sql, query.selectionArgs)
      try OptionRight apply i.fromCursor(cursor)
      finally cursor.close()
    } catch {
      case e: SQLException => OptionLeft(e)
    }
  }
}

class RecordSelector[A](val db: SQLiteDatabase) extends AnyVal {

  def selectBy[X: I, I[T] <: RecordIdentifiable[T]](id: X)
      (implicit i: RecordSelectable[I, A]): i.Result[A] = {

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
