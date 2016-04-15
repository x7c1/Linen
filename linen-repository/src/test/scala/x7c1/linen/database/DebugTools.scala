package x7c1.linen.database

import android.database.Cursor
import org.robolectric.RuntimeEnvironment
import x7c1.linen.database.control.DatabaseHelper

object DebugTools {
  def showTable(tableName: String) = {
    println(s"====== tableName : $tableName")
    val context = RuntimeEnvironment.application
    val helper = new DatabaseHelper(context)
    val db = helper.getReadableDatabase
    val cursor = db.rawQuery(s"SELECT * FROM $tableName LIMIT 100", Array())
    dumpCursor(cursor)
  }
  def dumpCursor(cursor: Cursor) = {
    println("=====")
    while(cursor.moveToNext()){
      println("-----")
      (0 to cursor.getColumnCount - 1) foreach { i =>
        val column = cursor.getColumnName(i)
        val value = cursor.getString(i)
        println(s"$column : $value")
      }
    }
  }
  def toMaps(cursor: Cursor): Seq[Map[String, String]] = {
    (0 to cursor.getCount - 1) map { i =>
      cursor moveToPosition i
      val pairs = (0 to cursor.getColumnCount - 1) map { i =>
        val column = cursor.getColumnName(i)
        val value = cursor.getString(i)
        column -> value
      }
      pairs.toMap
    }
  }
  def prettyPrint(target: Map[String, String]) = {
    target.
      map{ case (k, v) => s"$k -> $v" }.
      map("  " + _).
      mkString("Map(\n", ",\n", "\n)")
  }
}
