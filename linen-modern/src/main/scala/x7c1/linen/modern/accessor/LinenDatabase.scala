package x7c1.linen.modern.accessor

import android.content.{ContentValues, Context}
import android.database.sqlite.{SQLiteDatabase, SQLiteOpenHelper}
import android.database.{Cursor, SQLException}
import x7c1.wheat.macros.database.{TypedFields, TypedCursor}


object LinenDatabase {
  val name: String = "linen-db"
  val version = 1
}

class LinenOpenHelper(context: Context)
  extends SQLiteOpenHelper(context, LinenDatabase.name, null, LinenDatabase.version) {

  lazy val writableDatabase = new WritableDatabase(getWritableDatabase)
  lazy val readable = new ReadableDatabase(getReadableDatabase)

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
         |created_at INTEGER NOT NULL,
         |UNIQUE(url)
         |)""".stripMargin,

      s"""CREATE INDEX sources_created_at ON sources (
         |created_at)""".stripMargin
    )
    val sourceRatings = Seq(
      s"""CREATE TABLE IF NOT EXISTS source_ratings (
         |source_id INTEGER NOT NULL,
         |account_id INTEGER NOT NULL,
         |rating INTEGER NOT NULL,
         |created_at INTEGER NOT NULL,
         |UNIQUE(account_id, source_id),
         |FOREIGN KEY(source_id) REFERENCES sources(_id) ON DELETE CASCADE,
         |FOREIGN KEY(account_id) REFERENCES accounts(_id) ON DELETE CASCADE
         |)""".stripMargin,

      s"""CREATE INDEX source_ratings_id ON source_ratings (
         |account_id, rating, source_id)""".stripMargin
    )
    val entries = Seq(
      s"""CREATE TABLE IF NOT EXISTS entries (
         |_id INTEGER PRIMARY KEY AUTOINCREMENT,
         |source_id INTEGER NOT NULL,
         |url TEXT NOT NULL,
         |title TEXT NOT NULL,
         |content TEXT NOT NULL,
         |created_at INTEGER NOT NULL,
         |UNIQUE(url, source_id),
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
    val retrievedSourceMarks = Seq(
      s"""CREATE TABLE IF NOT EXISTS retrieved_source_marks (
         |source_id INTEGER NOT NULL,
         |latest_entry_id INTEGER NOT NULL,
         |updated_at INTEGER NOT NULL,
         |UNIQUE(source_id),
         |FOREIGN KEY(source_id) REFERENCES sources(_id) ON DELETE CASCADE,
         |FOREIGN KEY(latest_entry_id) REFERENCES entries(_id) ON DELETE CASCADE
         |)""".stripMargin,

      s"""CREATE INDEX retrieved_source_marks_created_at ON retrieved_source_marks (
         |updated_at)""".stripMargin,

      s"""CREATE TRIGGER update_source_marks AFTER INSERT ON entries
         |BEGIN
         |  INSERT OR REPLACE INTO retrieved_source_marks
         |      (source_id, latest_entry_id, updated_at)
         |    VALUES
         |      (new.source_id, new._id, strftime("%s", CURRENT_TIMESTAMP));
         |END
       """.stripMargin
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
      retrievedSourceMarks,
      sourceStatuses
    ).flatten foreach db.execSQL

  }
}

trait Insertable[A] {
  def tableName: String
  def toContentValues(target: A): ContentValues
}

object WritableDatabase {
  def transaction[A]
    (db: SQLiteDatabase)
    (writable: WritableDatabase => Either[SQLException, A]): Either[SQLException, A] = {

    try {
      db.beginTransaction()
      val result = writable(new WritableDatabase(db))
      if (result.isRight){
        db.setTransactionSuccessful()
      }
      result
    } finally {
      db.endTransaction()
    }
  }
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
  def replace[A: Insertable](target: A): Either[SQLException, Long] = {
    try {
      val i = implicitly[Insertable[A]]
      val id = db.replaceOrThrow(i.tableName, null, i toContentValues target)
      Right(id)
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

class ReadableDatabase(db: SQLiteDatabase) {
  def find[A]: SingleSelector[A] = new SingleSelector[A](db)
}

class SingleSelector[A](db: SQLiteDatabase){
  private type Selectable[X] = SingleSelectable[A, X]

  def apply[B: Selectable](id: B): Either[SQLException, Option[A]] = {
    try {
      val i = implicitly[Selectable[B]]
      val clause = i.where(id) map { case (key, _) => s"$key = ?" } mkString " AND "
      val sql = s"SELECT * FROM ${i.tableName} WHERE $clause"
      val args = i.where(id) map { case (_, value) => value }
      val cursor = db.rawQuery(sql, args.toArray)
      try Right apply i.fromCursor(cursor)
      finally cursor.close()
    } catch {
      case e: SQLException => Left(e)
    }
  }
}

trait SingleSelectable[A, ID] {
  def tableName: String
  def where(id: ID): Seq[(String, String)]
  def fromCursor(cursor: Cursor): Option[A]
}

class Query(
  val sql: String,
  val selectionArgs: Array[String]){

  def toExplain: Query = new Query(
    "EXPLAIN QUERY PLAN " + sql,
    selectionArgs
  )
  override def toString =
    s"""sql: $sql, args: ${selectionArgs.mkString(",")}"""
}

trait QueryPlanColumn extends TypedFields {
  def detail: String
}
case class QueryPlan(detail: String)

class QueryExplainer(db: SQLiteDatabase){
  def explain(query: Query): Seq[QueryPlan] = {
    val rawCursor = db.rawQuery(query.toExplain.sql, query.selectionArgs)
    val cursor = TypedCursor[QueryPlanColumn](rawCursor)
    try {
      (0 to rawCursor.getCount - 1) flatMap { n =>
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