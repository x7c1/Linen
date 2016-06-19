package x7c1.wheat.modern.database

import android.database.sqlite.SQLiteDatabase
import x7c1.wheat.macros.database.{Query, TypedCursor, TypedFields}


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
