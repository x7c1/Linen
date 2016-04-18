package x7c1.linen.database.control

import android.content.Context
import android.database.sqlite.{SQLiteDatabase, SQLiteOpenHelper}
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.database.{ReadableDatabase, WritableDatabase}

class DatabaseHelper(context: Context)
  extends SQLiteOpenHelper(context, LinenDatabase.name, null, LinenDatabase.version) {

  lazy val writable = new WritableDatabase(getWritableDatabase)

  lazy val readable = new ReadableDatabase(getReadableDatabase)

  override def onConfigure(db: SQLiteDatabase) = {
    db.setForeignKeyConstraintsEnabled(true)
  }

  override def onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int): Unit = {
    Log info s"[init] $oldVersion -> $newVersion"

    val upgrades = LinenDatabase upgradesFrom oldVersion
    val procedure = for {
      upgrade <- upgrades.view
      _ = Log info s"version:${upgrade.version}"
      query <- upgrade.queries
    } yield {
      Log info s"query: $query"
      db execSQL query
    }
    procedure.force
  }

  override def onCreate(db: SQLiteDatabase): Unit = {
    Log info "[init]"
    LinenDatabase.defaults foreach { query =>
      Log info s"query: $query"
      db execSQL query
    }
    onUpgrade(db, 0, LinenDatabase.version)
  }
}