package x7c1.wheat.modern.database

import android.database.SQLException
import android.database.sqlite.SQLiteDatabase


class WritableDatabase(db: SQLiteDatabase) {
  def insert[A: Insertable](target: A): Either[SQLException, Long] = {
    try {
      val i = implicitly[Insertable[A]]
      val id = db.insertOrThrow(i.tableName, null, i toContentValues target)
      Right(id)
    } catch {
      case e: SQLException => Left(e)
    }
  }
  def update[A: Updatable](target: A): Either[SQLException, Int] = {
    try {
      val updatable = implicitly[Updatable[A]]
      val where = updatable where target
      val clause = where map { case (key, _) => s"$key = ?" }
      val args = where map { case (_, value) => value }
      Right apply db.update(
        updatable.tableName,
        updatable toContentValues  target,
        clause mkString " AND ",
        args.toArray
      )
    } catch {
      case e: SQLException => Left(e)
    }
  }
  def replace[A: Insertable](target: A): Either[SQLException, Long] = {
    try {
      val i = implicitly[Insertable[A]]
      val id = db.replaceOrThrow(i.tableName, null, i toContentValues target)
      Right(id)
    } catch {
      case e: SQLException => Left(e)
    }
  }
  def delete[A: Deletable](target: A): Either[SQLException, Int] = {
    try {
      val updatable = implicitly[Deletable[A]]
      val where = updatable where target
      val clause = where map { case (key, _) => s"$key = ?" }
      val args = where map { case (_, value) => value }
      Right apply db.delete(
        updatable.tableName,
        clause mkString " AND ",
        args.toArray
      )
    } catch {
      case e: SQLException => Left(e)
    }
  }
  def truncate[A: HasTable: AllowTruncate]: Either[SQLException, Int] = {
    try {
      Right apply db.delete(
        implicitly[HasTable[A]].tableName,
        "",
        Array()
      )
    } catch {
      case e: SQLException => Left(e)
    }
  }
}

object WritableDatabase {
  def transaction[A, ERROR]
    (db: SQLiteDatabase)
    (writable: WritableDatabase => Either[ERROR, A]): Either[ERROR, A] = {

    try {
      db.beginTransaction()
      val result = writable(new WritableDatabase(db))
      if (result.isRight){
        db.setTransactionSuccessful()
      }
      result
    } finally {
      db.endTransaction()
    }
  }
}
