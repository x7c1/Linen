package x7c1.linen.modern.accessor

import android.content.Context
import android.database.sqlite.{SQLiteDatabase, SQLiteOpenHelper}


object LinenDatabase {
  val name: String = "linen-db"
  val version = 1
}

class LinenOpenHelper(context: Context)
  extends SQLiteOpenHelper(context, LinenDatabase.name, null, LinenDatabase.version) {

  override def onConfigure(db: SQLiteDatabase) = {
    db.setForeignKeyConstraintsEnabled(true)
  }

  override def onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int): Unit = {
  }

  override def onCreate(db: SQLiteDatabase): Unit = {
    db.execSQL(
      s"""CREATE TABLE IF NOT EXISTS accounts (
         |_id INTEGER PRIMARY KEY AUTOINCREMENT,
         |nickname TEXT,
         |biography TEXT,
         |created_at INTEGER
         |)""".stripMargin
    )
    db.execSQL(
      s"""CREATE INDEX accounts_created_at ON accounts (
         |created_at)""".stripMargin
    )
    db.execSQL(
      s"""CREATE TABLE IF NOT EXISTS sources (
         |_id INTEGER PRIMARY KEY AUTOINCREMENT,
         |url TEXT,
         |title TEXT,
         |description TEXT,
         |created_at INTEGER
         |)""".stripMargin
    )
    db.execSQL(
      s"""CREATE INDEX sources_created_at ON sources (
         |created_at)""".stripMargin
    )
    /*
    db.execSQL(
      s"""CREATE INDEX sources_rating ON sources (
         |rating,created_at)""".stripMargin
    )
    db.execSQL(
    */
      s"""CREATE TABLE IF NOT EXISTS entries (
         |_id INTEGER PRIMARY KEY AUTOINCREMENT,
         |source_id INTEGER,
         |url TEXT,
         |title TEXT,
         |content TEXT,
         |created_at INTEGER,
         |FOREIGN KEY(source_id) REFERENCES sources(_id) ON DELETE CASCADE
         |)""".stripMargin
    )
    db.execSQL(
      s"""CREATE INDEX entries_source_created_at ON entries (
         |source_id, created_at)""".stripMargin
    )
    db.execSQL(
      s"""CREATE INDEX entries_created_at ON entries (
         |created_at)""".stripMargin
    )
    db.execSQL(
      s"""CREATE TABLE IF NOT EXISTS lists (
         |_id INTEGER PRIMARY KEY AUTOINCREMENT,
         |account_id INTEGER,
         |name TEXT,
         |description TEXT,
         |created_at INTEGER,
         |FOREIGN KEY(account_id) REFERENCES accounts(_id) ON DELETE CASCADE
         |)""".stripMargin
    )
    db.execSQL(
      s"""CREATE TABLE IF NOT EXISTS list_source_map (
         |list_id INTEGER,
         |source_id INTEGER,
         |created_at INTEGER,
         |FOREIGN KEY(source_id) REFERENCES sources(_id) ON DELETE CASCADE,
         |FOREIGN KEY(list_id) REFERENCES lists(_id) ON DELETE CASCADE
         |)""".stripMargin
    )
    db.execSQL(
      s"""CREATE INDEX list_source_created_at ON list_source_map (
         |list_id, created_at)""".stripMargin
    )
    db.execSQL(
      s"""CREATE INDEX list_source_sid ON list_source_map (
         |source_id)""".stripMargin
    )
    db.execSQL(
      s"""CREATE TABLE IF NOT EXISTS source_statuses (
         |source_id INTEGER NOT NULL,
         |last_entry_id INTEGER,
         |account_id INTEGER NOT NULL,
         |rating INTEGER NOT NULL,
         |created_at INTEGER,
         |FOREIGN KEY(source_id) REFERENCES sources(_id) ON DELETE CASCADE,
         |FOREIGN KEY(last_entry_id) REFERENCES entries(_id) ON DELETE CASCADE
         |FOREIGN KEY(account_id) REFERENCES accounts(_id) ON DELETE CASCADE
         |)""".stripMargin
    )

  }
}
