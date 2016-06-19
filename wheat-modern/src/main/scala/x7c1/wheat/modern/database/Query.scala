package x7c1.wheat.modern.database

import android.database.sqlite.SQLiteDatabase
import x7c1.wheat.macros.database.{TypedCursor, TypedFields}

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

object Query {
  def apply(sql: String, selectionArgs: Array[String] = Array()): Query = {
    new Query(sql, selectionArgs)
  }
}

trait QueryPlanColumn extends TypedFields {
  def detail: String
}
case class QueryPlan(detail: String){
  def useTempBtree: Boolean = {
    detail contains "USE TEMP B-TREE"
  }
}

class QueryExplainer(db: SQLiteDatabase){
  def explain(query: Query): Seq[QueryPlan] = {
    val rawCursor = db.rawQuery(query.toExplain.sql, query.selectionArgs)
    val cursor = TypedCursor[QueryPlanColumn](rawCursor)
    try {
      0 until rawCursor.getCount flatMap { n =>
        cursor.moveToFind(n){
          QueryPlan(detail = cursor.detail)
        }
      }
    } finally {
      rawCursor.close()
    }
  }
}

object QueryExplainer {
  def apply(db: SQLiteDatabase): QueryExplainer = new QueryExplainer(db)
}
