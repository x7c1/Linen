package x7c1.linen.modern.accessor

import android.content.Context
import android.database.sqlite.{SQLiteDatabase, SQLiteOpenHelper}


object LinenDatabase {
  val name: String = "linen-db"
  val version = 1
}

class LinenOpenHelper(context: Context)
  extends SQLiteOpenHelper(context, LinenDatabase.name, null, LinenDatabase.version) {

  override def onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int): Unit = {
  }

  override def onCreate(db: SQLiteDatabase): Unit = {
    db.execSQL(
      s"""CREATE TABLE IF NOT EXISTS sources (
         |_id INTEGER PRIMARY KEY AUTOINCREMENT,
         |url TEXT,
         |title TEXT,
         |description TEXT,
         |rating INTEGER,
         |created_at INTEGER
         |)""".stripMargin
    )
    db.execSQL(
      s"""CREATE INDEX sources_created_at ON sources (
         |created_at)""".stripMargin
    )
    db.execSQL(
      s"""CREATE INDEX sources_rating ON sources (
         |rating,created_at)""".stripMargin
    )
    db.execSQL(
      s"""CREATE TABLE IF NOT EXISTS entries (
         |_id INTEGER PRIMARY KEY AUTOINCREMENT,
         |source_id INTEGER,
         |url TEXT,
         |title TEXT,
         |content TEXT,
         |read_state INTEGER,
         |created_at INTEGER
         |)""".stripMargin
    )
    db.execSQL(
      s"""CREATE INDEX entries_source_id ON entries (
         |source_id)""".stripMargin
    )
    db.execSQL(
      s"""CREATE INDEX entries_created_at ON entries (
         |created_at)""".stripMargin
    )

    db.execSQL(
      s"""CREATE TABLE IF NOT EXISTS list_source_map (
         |list_id INTEGER,
         |source_id INTEGER,
         |created_at INTEGER
         |)""".stripMargin
    )
    db.execSQL(
      s"""CREATE INDEX list_source_id ON list_source_map (
         |list_id, source_id)""".stripMargin
    )
    db.execSQL(
      s"""CREATE INDEX list_source_sid ON list_source_map (
         |source_id)""".stripMargin
    )

  }
}
