package x7c1.linen.modern.accessor

import android.content.{ContentProvider, ContentUris, ContentValues, Context, UriMatcher}
import android.database.Cursor
import android.database.sqlite.{SQLiteDatabase, SQLiteOpenHelper, SQLiteQueryBuilder}
import android.net.Uri


object LinenDatabase {
  val name: String = "linen-db"
  val version = 1
}

object SourceProvider {

  private val UriPath = "source"

  private val Authority = "x7c1.linen.provider.source"

  val ContentUri = Uri.parse(s"content://$Authority/$UriPath")
}

class SourceProvider extends ContentProvider {
  import SourceProvider.{Authority, ContentUri, UriPath}
  import x7c1.linen.modern.accessor.SourcesTable.tableName

  private val CodeSources = 1

  private val CodeSourceId = 2

  private lazy val uriMatcher = {
    val matcher = new UriMatcher(UriMatcher.NO_MATCH)
    matcher.addURI(Authority, UriPath, CodeSources)
    matcher.addURI(Authority, UriPath + "/#", CodeSourceId)
    matcher
  }

  private lazy val helper = new LinenOpenHelper(getContext)

  override def onCreate(): Boolean = {
    true
  }

  override def getType(uri: Uri): String = {

    // rf.
    // http://developer.android.com/guide/topics/providers/content-provider-creating.html#TableMIMETypes

    uriMatcher `match` uri match {
      case CodeSourceId => s"vnd.android.cursor.item/$Authority.$tableName"
      case CodeSources => s"vnd.android.cursor.dir/$Authority.$tableName"
    }
  }

  override def update(
    uri: Uri, values: ContentValues,
    selection: String, selectionArgs: Array[String]): Int = {

    val db = helper.getWritableDatabase
    //val id = uri.getPathSegments.get(1)
    val count = db.update(tableName, values, selection, selectionArgs)
    getContext.getContentResolver.notifyChange(uri, null)
    count
  }

  override def insert(uri: Uri, values: ContentValues): Uri = {
    val db = helper.getWritableDatabase
    val insertedId = db.insert(tableName, null, values)
    val newUri = ContentUris.withAppendedId(ContentUri, insertedId)
    getContext.getContentResolver.notifyChange(newUri, null)
    newUri
  }

  override def delete(uri: Uri, selection: String, selectionArgs: Array[String]): Int = {
    val db = helper.getWritableDatabase
    val count = db.delete(tableName, selection, selectionArgs)
    getContext.getContentResolver.notifyChange(uri, null)
    count
  }

  override def query(
    uri: Uri, projection: Array[String],
    selection: String, selectionArgs: Array[String], sortOrder: String): Cursor = {

    val builder = new SQLiteQueryBuilder

    uriMatcher `match` uri match {
      case CodeSources | CodeSourceId =>
        builder.setTables(tableName)
      case _ =>
        throw new IllegalArgumentException(s"invalid uri:$uri")
    }
    val db = helper.getReadableDatabase
    builder.query(db, projection, selection, selectionArgs, null, null, sortOrder)
  }
}

object SourcesTable {
  val tableName: String = "sources"
}

object EntryProvider {

  private val UriPath = "entry"

  private val Authority = "x7c1.linen.provider.entry"

  val ContentUri = Uri.parse(s"content://$Authority/$UriPath")
}

class EntryProvider extends ContentProvider {

  import EntriesTable.tableName
  import EntryProvider.{Authority, ContentUri, UriPath}

  private val CodeEntries = 1

  private val CodeEntryId = 2

  private lazy val uriMatcher = {
    val matcher = new UriMatcher(UriMatcher.NO_MATCH)
    matcher.addURI(Authority, UriPath, CodeEntries)
    matcher.addURI(Authority, UriPath + "/#", CodeEntryId)
    matcher
  }

  private lazy val helper = new LinenOpenHelper(getContext)

  override def onCreate(): Boolean = {
    true
  }

  override def getType(uri: Uri): String = {

    // rf.
    // http://developer.android.com/guide/topics/providers/content-provider-creating.html#TableMIMETypes

    uriMatcher `match` uri match {
      case CodeEntryId => s"vnd.android.cursor.item/$Authority.$tableName"
      case CodeEntries => s"vnd.android.cursor.dir/$Authority.$tableName"
    }
  }

  override def update(
    uri: Uri, values: ContentValues,
    selection: String, selectionArgs: Array[String]): Int = {

    val db = helper.getWritableDatabase
    //val id = uri.getPathSegments.get(1)
    val count = db.update(tableName, values, selection, selectionArgs)
    getContext.getContentResolver.notifyChange(uri, null)
    count
  }

  override def insert(uri: Uri, values: ContentValues): Uri = {
    val db = helper.getWritableDatabase
    val insertedId = db.insert(tableName, null, values)
    val newUri = ContentUris.withAppendedId(ContentUri, insertedId)
    getContext.getContentResolver.notifyChange(newUri, null)
    newUri
  }

  override def delete(uri: Uri, selection: String, selectionArgs: Array[String]): Int = {
    val db = helper.getWritableDatabase
    val count = db.delete(tableName, selection, selectionArgs)
    getContext.getContentResolver.notifyChange(uri, null)
    count
  }

  override def query(
    uri: Uri, projection: Array[String],
    selection: String, selectionArgs: Array[String], sortOrder: String): Cursor = {

    val builder = new SQLiteQueryBuilder

    uriMatcher `match` uri match {
      case CodeEntries | CodeEntryId =>
        builder.setTables(tableName)
      case _ =>
        throw new IllegalArgumentException(s"invalid uri:$uri")
    }
    val db = helper.getReadableDatabase
    builder.query(db, projection, selection, selectionArgs, null, null, sortOrder)
  }
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
         |rating INTEGER
         |)""".stripMargin
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
      s"""CREATE TABLE IF NOT EXISTS list_source_map (
         |list_id INTEGER,
         |source_id INTEGER,
         |created_at INTEGER
         |)""".stripMargin

    )
  }
}

object EntriesTable {
  val tableName: String = "entries"
}
