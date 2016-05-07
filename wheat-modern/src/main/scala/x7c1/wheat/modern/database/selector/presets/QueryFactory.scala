package x7c1.wheat.modern.database.selector.presets

import x7c1.wheat.modern.database.Query
import x7c1.wheat.modern.database.selector.CanIdentify

import scala.language.higherKinds

private case class QueryFactory[I[T] <: CanIdentify[T]](table: String){
  def create[X: I](target: X)(where: I[X]#ID => Seq[(String, String)]): Query = {
    val id = implicitly[I[X]] toId target
    val clause = where(id) map { case (key, _) => s"$key = ?" } mkString " AND "
    val sql = s"SELECT * FROM $table WHERE $clause"
    val args = where(id) map { case (_, value) => value }
    new Query(sql, args.toArray)
  }
}
