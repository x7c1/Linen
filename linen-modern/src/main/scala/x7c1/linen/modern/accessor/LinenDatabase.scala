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
    val accounts = Seq(
      s"""CREATE TABLE IF NOT EXISTS accounts (
         |_id INTEGER PRIMARY KEY AUTOINCREMENT,
         |nickname TEXT,
         |biography TEXT,
         |created_at INTEGER
         |)""".stripMargin,

      s"""CREATE INDEX accounts_created_at ON accounts (
         |created_at)""".stripMargin
    )
    val sources = Seq(
      s"""CREATE TABLE IF NOT EXISTS sources (
         |_id INTEGER PRIMARY KEY AUTOINCREMENT,
         |url TEXT,
         |title TEXT NOT NULL,
         |description TEXT NOT NULL,
         |created_at INTEGER
         |)""".stripMargin,

      s"""CREATE INDEX sources_created_at ON sources (
         |created_at)""".stripMargin
    )
    val sourceRatings = Seq(
      s"""CREATE TABLE IF NOT EXISTS source_ratings (
         |source_id INTEGER NOT NULL,
         |owner_account_id INTEGER NOT NULL,
         |rating INTEGER NOT NULL,
         |created_at INTEGER,
         |UNIQUE(owner_account_id, source_id),
         |FOREIGN KEY(source_id) REFERENCES sources(_id) ON DELETE CASCADE,
         |FOREIGN KEY(owner_account_id) REFERENCES accounts(_id) ON DELETE CASCADE
         |)""".stripMargin,

      s"""CREATE INDEX source_ratings_id ON source_ratings (
         |owner_account_id, rating, source_id)""".stripMargin
    )
    val entries = Seq(
      s"""CREATE TABLE IF NOT EXISTS entries (
         |_id INTEGER PRIMARY KEY AUTOINCREMENT,
         |source_id INTEGER,
         |url TEXT,
         |title TEXT,
         |content TEXT,
         |created_at INTEGER,
         |FOREIGN KEY(source_id) REFERENCES sources(_id) ON DELETE CASCADE
         |)""".stripMargin,

      s"""CREATE INDEX entries_source ON entries (
         |source_id, _id)""".stripMargin,

      s"""CREATE INDEX entries_source_created_at ON entries (
         |source_id, created_at)""".stripMargin,

      s"""CREATE INDEX entries_created_at ON entries (
         |created_at)""".stripMargin
    )
    val lists = Seq(
      s"""CREATE TABLE IF NOT EXISTS lists (
         |_id INTEGER PRIMARY KEY AUTOINCREMENT,
         |account_id INTEGER,
         |name TEXT,
         |description TEXT,
         |created_at INTEGER,
         |FOREIGN KEY(account_id) REFERENCES accounts(_id) ON DELETE CASCADE
         |)""".stripMargin
    )
    val listSourceMap = Seq(
      s"""CREATE TABLE IF NOT EXISTS list_source_map (
         |list_id INTEGER NOT NULL,
         |source_id INTEGER NOT NULL,
         |created_at INTEGER,
         |UNIQUE(list_id, source_id),
         |FOREIGN KEY(source_id) REFERENCES sources(_id) ON DELETE CASCADE,
         |FOREIGN KEY(list_id) REFERENCES lists(_id) ON DELETE CASCADE
         |)""".stripMargin
    )
    val sourceStatuses = Seq(
      s"""CREATE TABLE IF NOT EXISTS source_statuses (
         |source_id INTEGER NOT NULL,
         |start_entry_id INTEGER,
         |account_id INTEGER NOT NULL,
         |created_at INTEGER,
         |UNIQUE(source_id, account_id),
         |FOREIGN KEY(source_id) REFERENCES sources(_id) ON DELETE CASCADE,
         |FOREIGN KEY(start_entry_id) REFERENCES entries(_id) ON DELETE CASCADE
         |FOREIGN KEY(account_id) REFERENCES accounts(_id) ON DELETE CASCADE
         |)""".stripMargin
    )
    Seq(
      accounts,
      sources,
      sourceRatings,
      entries,
      lists,
      listSourceMap,
      sourceStatuses
    ).flatten foreach db.execSQL

  }
}
