package x7c1.wheat.modern.database.selector.presets

import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import x7c1.wheat.modern.database.selector.{CanIdentify, ItemSelector, SelectorProvidable}
import x7c1.wheat.modern.either.Imports._
import x7c1.wheat.modern.either.OptionEither

import scala.language.higherKinds

trait CollectFrom [I[T] <: CanIdentify[T], A]{
  protected def db: SQLiteDatabase

  def collectFrom[X: I](target: X)
      (implicit i: CanCollect[I, A]): Either[SQLException, Seq[A]] = {

    ItemSelector(db) selectBy target
  }
}

trait TraverseOn[I[T] <: CanIdentify[T], A]{
  protected def db: SQLiteDatabase

  def traverseOn[X: I](target: X)
      (implicit i: CanTraverse[I, A]): Either[SQLException, CursorClosableSequence[A]] = {

    ItemSelector(db) selectBy target
  }
}

trait Find[A]{
  protected def db: SQLiteDatabase

  def find()(implicit i: CanFindByQuery[A]): OptionEither[SQLException, A] = {
    val either = ItemSelector(db).select
    either.toOptionEither
  }
}
object Find {
  type FindProvidable[A] = SelectorProvidable[A, _ <: Find[A]]
}

trait FindBy[I[T] <: CanIdentify[T], A]{
  protected def db: SQLiteDatabase

  def findBy[X: I](target: X)
      (implicit i: CanFind[I, A]): OptionEither[SQLException, A] = {

    val either = ItemSelector(db) selectBy target
    either.toOptionEither
  }
}

trait FindByTag[I[T] <: CanIdentify[T], A]{
  protected def db: SQLiteDatabase

  def findByTag[X: I](target: X)
      (implicit i: CanFind[I, A]): OptionEither[SQLException, A] = {

    val either = ItemSelector(db) selectBy target
    either.toOptionEither
  }
}
