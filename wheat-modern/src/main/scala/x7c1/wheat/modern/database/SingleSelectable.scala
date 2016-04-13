package x7c1.wheat.modern.database

import android.database.Cursor

trait SingleSelectable[A, ID] {
  def query(id: ID): Query
  def fromCursor(cursor: Cursor): Option[A]
}

abstract class SingleWhere[A, ID](table: String) extends SingleSelectable[A, ID]{

  def where(id: ID): Seq[(String, String)]

  override def query(id: ID): Query = {
    val clause = where(id) map { case (key, _) => s"$key = ?" } mkString " AND "
    val sql = s"SELECT * FROM $table WHERE $clause"
    val args = where(id) map { case (_, value) => value }
    new Query(sql, args.toArray)
  }
}

abstract class ZeroAritySingle[A](select: Query) extends SingleSelectable[A, Unit]{
  override def query(id: Unit): Query = select
}
