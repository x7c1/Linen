package x7c1.wheat.modern.database.presets

import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import x7c1.wheat.modern.database.{RecordFindable, RecordIdentifiable, RecordSelector, SeqSelectable}
import x7c1.wheat.modern.either.Imports._
import x7c1.wheat.modern.either.OptionEither

import scala.language.higherKinds

trait CollectFrom [I[T] <: RecordIdentifiable[T], A]{
  protected def db: SQLiteDatabase

  def collectFrom[X: I](target: X)
      (implicit i: SeqSelectable[I, A]): Either[SQLException, Seq[A]] = {

    RecordSelector(db) selectBy target
  }
}

trait Find[I[T] <: RecordIdentifiable[T], A]{
  protected def db: SQLiteDatabase

  def find[X: I](target: X)
      (implicit i: RecordFindable[I, A]): OptionEither[SQLException, A] = {

    val either = RecordSelector(db) selectBy target
    either.toOptionEither
  }
}
