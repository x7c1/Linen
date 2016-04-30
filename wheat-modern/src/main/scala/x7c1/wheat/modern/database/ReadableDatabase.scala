package x7c1.wheat.modern.database

import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import x7c1.wheat.modern.either.{OptionEither, OptionLeft, OptionRight}

import scala.language.higherKinds


class ReadableDatabase(db: SQLiteDatabase) {

  def find[A]: SingleSelector[A] = new SingleSelector[A](db)

  def select[A]: MultipleSelector[A] = new MultipleSelector[A](db)

  def selectorOf[A](implicit x: SelectorFindable[A]): x.Selector = x selectorFrom this
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

class MultipleSelector[A](db: SQLiteDatabase){

  def by[B](id: B)(implicit i: MultipleSelectable[A, B]): i.Result[A] = {
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

trait SelectorFindable[A]{
  type Selector
  def selectorFrom(db: ReadableDatabase): Selector
}
