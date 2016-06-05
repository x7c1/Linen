package x7c1.wheat.modern.database.selector

import android.database.sqlite.SQLiteDatabase

import scala.language.higherKinds

class ItemSelector[A](val db: SQLiteDatabase) extends AnyVal {

  def selectBy[X: I, I[T] <: CanIdentify[T]](id: X)(implicit i: CanExtract[I, A]): i.Result[A] = {
    i.extract(db, id)
  }
  def select(implicit i: CanSelect[UnitIdentifiable, A]): i.Result[A] = {
    selectBy[Unit, UnitIdentifiable]({})
  }
}

object ItemSelector {
  def apply[A](db: SQLiteDatabase): ItemSelector[A] = {
    new ItemSelector[A](db)
  }
}
