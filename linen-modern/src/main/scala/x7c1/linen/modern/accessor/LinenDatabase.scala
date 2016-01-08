package x7c1.linen.modern.accessor

import android.content.{ContentValues, Context}
import android.database.SQLException
import android.database.sqlite.{SQLiteDatabase, SQLiteOpenHelper}


object LinenDatabase {
  val name: String = "linen-db"
  val version = 1
}

class LinenOpenHelper(context: Context)
  extends SQLiteOpenHelper(context, LinenDatabase.name, null, LinenDatabase.version) {

  lazy val writableDatabase = new WritableDatabase(getWritableDatabase)

  override def onConfigure(db: SQLiteDatabase) = {
    db.setForeignKeyConstraintsEnabled(true)
  }

  override def onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int): Unit = {
  }

  override def onCreate(db: SQLiteDatabase): Unit = {
    val accounts = Seq(
      s"""CREATE TABLE IF NOT EXISTS accounts (
         |_id INTEGER PRIMARY KEY AUTOINCREMENT,
         |nickname TEXT NOT NULL,
         |biography TEXT NOT NULL,
         |created_at INTEGER NOT NULL
         |)""".stripMargin,

      s"""CREATE INDEX accounts_created_at ON accounts (
         |created_at)""".stripMargin
    )
    val sources = Seq(
      s"""CREATE TABLE IF NOT EXISTS sources (
         |_id INTEGER PRIMARY KEY AUTOINCREMENT,
         |url TEXT NOT NULL,
         |title TEXT NOT NULL,
         |description TEXT NOT NULL,
         |created_at INTEGER NOT NULL
         |)""".stripMargin,

      s"""CREATE INDEX sources_created_at ON sources (
         |created_at)""".stripMargin
    )
    val sourceRatings = Seq(
      s"""CREATE TABLE IF NOT EXISTS source_ratings (
         |source_id INTEGER NOT NULL,
         |owner_account_id INTEGER NOT NULL,
         |rating INTEGER NOT NULL,
         |created_at INTEGER NOT NULL,
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
         |source_id INTEGER NOT NULL,
         |url TEXT NOT NULL,
         |title TEXT NOT NULL,
         |content TEXT NOT NULL,
         |created_at INTEGER NOT NULL,
         |FOREIGN KEY(source_id) REFERENCES sources(_id) ON DELETE CASCADE
         |)""".stripMargin,

      s"""CREATE INDEX entries_source ON entries (
         |source_id, _id)""".stripMargin,

      s"""CREATE INDEX entries_source_created_at ON entries (
         |source_id, created_at)""".stripMargin,

      s"""CREATE INDEX entries_created_at ON entries (
         |created_at)""".stripMargin
    )
    val channels = Seq(
      s"""CREATE TABLE IF NOT EXISTS channels (
         |_id INTEGER PRIMARY KEY AUTOINCREMENT,
         |account_id INTEGER NOT NULL,
         |name TEXT NOT NULL,
         |description TEXT NOT NULL,
         |created_at INTEGER NOT NULL,
         |FOREIGN KEY(account_id) REFERENCES accounts(_id) ON DELETE CASCADE
         |)""".stripMargin
    )
    val channelSourceMap = Seq(
      s"""CREATE TABLE IF NOT EXISTS channel_source_map (
         |channel_id INTEGER NOT NULL,
         |source_id INTEGER NOT NULL,
         |created_at INTEGER NOT NULL,
         |UNIQUE(channel_id, source_id),
         |FOREIGN KEY(source_id) REFERENCES sources(_id) ON DELETE CASCADE,
         |FOREIGN KEY(channel_id) REFERENCES channels(_id) ON DELETE CASCADE
         |)""".stripMargin
    )
    val sourceStatuses = Seq(
      s"""CREATE TABLE IF NOT EXISTS source_statuses (
         |source_id INTEGER NOT NULL,
         |start_entry_id INTEGER,
         |account_id INTEGER NOT NULL,
         |created_at INTEGER NOT NULL,
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
      channels,
      channelSourceMap,
      sourceStatuses
    ).flatten foreach db.execSQL

  }
}

trait Insertable[A] {
  def tableName: String
  def toContentValues(target: A): ContentValues
}

class WritableDatabase(db: SQLiteDatabase) {
  def insert[A: Insertable](target: A): Either[SQLException, Long] = {
    try {
      val i = implicitly[Insertable[A]]
      val id = db.insertOrThrow(i.tableName, null, i toContentValues target)
      Right(id)
    } catch {
      case e: SQLException => Left(e)
    }
  }
  def update[A: Updatable](target: A): Either[SQLException, Int] = {
    try {
      val updatable = implicitly[Updatable[A]]
      val where = updatable where target
      val clause = where map { case (key, _) => s"$key = ?" }
      val args = where map { case (_, value) => value }
      Right apply db.update(
        updatable.tableName,
        updatable toContentValues  target,
        clause mkString " AND ",
        args.toArray
      )
    } catch {
      case e: SQLException => Left(e)
    }

  }
}

trait Updatable[A] {
  def tableName: String
  def toContentValues(target: A): ContentValues
  def where(target: A): Seq[(String, String)]
}
