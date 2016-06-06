package x7c1.wheat.modern.database

import android.content.ContentValues

trait HasTable[A]{
  def tableName: String
}

trait Insertable[A] {
  def tableName: String
  def toContentValues(target: A): ContentValues
}

trait Updatable[A] {
  def tableName: String
  def toContentValues(target: A): ContentValues
  def where(target: A): Seq[(String, String)]
}

trait Deletable[A]{
  def tableName: String
  def where(target: A): Seq[(String, String)]
}

class AllowTruncate[A]
