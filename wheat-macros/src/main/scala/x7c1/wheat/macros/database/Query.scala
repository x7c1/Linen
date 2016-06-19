package x7c1.wheat.macros.database

import scala.collection.mutable.ArrayBuffer

object Query {
  def apply(sql: String, selectionArgs: Array[String] = Array()): Query = {
    new Query(sql, selectionArgs)
  }
  implicit class SqlBuilder(val context: StringContext) extends AnyVal {
    def sql(args: Any*): Query = {
      val strings = context.parts.iterator
      val expressions = args.iterator
      val buffer = new StringBuffer(strings.next().stripMargin)
      val selectionArgs = ArrayBuffer[String]()
      while(strings.hasNext){
        buffer append "?"
        buffer append strings.next().stripMargin
        selectionArgs append expressions.next().toString
      }
      Query(buffer.toString, selectionArgs.toArray)
    }
  }
}

class Query private (
  val sql: String,
  val selectionArgs: Array[String] = Array()) {

  def toExplain: Query = new Query(
    "EXPLAIN QUERY PLAN " + sql,
    selectionArgs
  )
  override def toString = {
    s"""sql: $sql, args: ${selectionArgs.mkString(",")}"""
  }
}
