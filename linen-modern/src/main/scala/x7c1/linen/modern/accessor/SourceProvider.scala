package x7c1.linen.modern.accessor

import android.content.{ContentProvider, ContentUris, ContentValues, Context, UriMatcher}
import android.database.Cursor
import android.database.sqlite.{SQLiteDatabase, SQLiteOpenHelper, SQLiteQueryBuilder}
import android.net.Uri
import x7c1.linen.modern.accessor.SourcesTable.{tableName, tableVersion}

object SourceProvider {

  private val UriPath = "source"

  private val Authority = "x7c1.linen.provider"

  val ContentUri = Uri.parse(s"content://$Authority/$UriPath")
}

class SourceProvider extends ContentProvider {

  import SourceProvider.{UriPath, Authority, ContentUri}

  private val CodeSources = 1

  private val CodeSourceId = 2

  private lazy val uriMatcher = {
    val matcher = new UriMatcher(UriMatcher.NO_MATCH)
    matcher.addURI(Authority, UriPath, CodeSources)
    matcher.addURI(Authority, UriPath + "/#", CodeSourceId)
    matcher
  }

  private lazy val helper = new SourceOpenHelper(getContext)

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

class SourceOpenHelper(context: Context)
  extends SQLiteOpenHelper(context, tableName, null, tableVersion) {

  override def onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int): Unit = {
  }

  override def onCreate(db: SQLiteDatabase): Unit = {
    db.execSQL(
      s"""CREATE TABLE $tableName (
         |_id INTEGER PRIMARY KEY AUTOINCREMENT,
         |url TEXT,
         |title TEXT,
         |description TEXT
         |)""".stripMargin
    )
  }
}

object SourcesTable {
  val tableVersion: Int = 1
  val tableName: String = "sources"
}
