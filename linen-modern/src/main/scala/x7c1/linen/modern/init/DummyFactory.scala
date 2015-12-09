package x7c1.linen.modern.init

import java.util.Date

import android.app.Activity
import android.content.{ContentValues, Context}
import android.widget.Toast
import x7c1.linen.glue.res.layout.MainLayout
import x7c1.linen.modern.accessor.{EntryProvider, LinenOpenHelper, SourceProvider}
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.Imports._
import x7c1.wheat.modern.patch.TaskAsync.async

object DummyFactory {
  def setup(layout: MainLayout, activity: Activity) = {

    val helpers = Seq(
      new LinenOpenHelper(activity)
    )
    helpers foreach { helper =>
      val db = helper.getWritableDatabase

      val iter = db.getAttachedDbs.listIterator()
      while(iter.hasNext){
        Log error "---"
        val pair = iter.next()
        Log error pair.first + " - " + pair.second
      }

      helper onCreate db
    }

    layout.createDummies onClick { _ =>
      async {
        (1 to 1000) foreach { n =>
          val source = new ContentValues()
          source.put("title", s"title $n hoge-")
          source.put("description", s"description $n fuga-" + new Date())
          val uri = activity.getContentResolver.insert(SourceProvider.ContentUri, source)
          val newSourceId = uri.getPathSegments.get(1)

          if (n % 10 == 0){
            Log error "" + uri.getPathSegments
          }

          (1 to 3) foreach { i =>
            val entry = new ContentValues()
            entry.put("title", s"$newSourceId-$i entry title" * 10)
            entry.put("content", s"$newSourceId-$i entry content " * 30)
            entry.put("source_id", newSourceId)
            activity.getContentResolver.insert(EntryProvider.ContentUri, entry)
          }
        }
        layout.createDummies runUi { _ =>
          Toast.makeText(activity, "dummies inserted", Toast.LENGTH_SHORT).show()
        }
      }
    }
    layout.showAllEntries onClick { _ =>
      async {
        val cursor = activity.getContentResolver.query(EntryProvider.ContentUri, null, null, null, null, null)
        activity.startManagingCursor(cursor)

        while(cursor.moveToNext()){
          (0 to cursor.getColumnCount - 1) foreach { i =>
            val column = cursor.getColumnName(i)
            val value = cursor.getString(i)
            Log error s"$column = $value"
          }
        }
      }
    }
    layout.initDummies onClick { _ =>
      val db = new LinenOpenHelper(activity).getWritableDatabase
      db.execSQL("DROP TABLE IF EXISTS sources")
      db.execSQL("DROP TABLE IF EXISTS entries")

      Log error s"dropped"
    }

    val values = new ContentValues()
    (1 to 3) foreach { n =>
      values.put("title", s"title $n hoge-")
      values.put("description", s"description $n fuga-" + new Date())
      activity.getContentResolver.insert(SourceProvider.ContentUri, values)
    }
  }

  def createDummies(context: Context) = {
    val helper = new LinenOpenHelper(context)
    val db = helper.getWritableDatabase
    (1 to 5) foreach { i =>
      val source = new ContentValues()
      source.put("title", s"title-$i")
      source.put("description", s"description-$i")
      val sourceId = db.insert("sources", null, source)

      val listId = 123
      val listSourceMap = new ContentValues()
      source.put("list_id", listId: Double)
      source.put("source_id", sourceId: Double)
      db.insert("list_source_map", null, listSourceMap)

      (1 to 3) foreach { j =>
        val entry = new ContentValues()
        entry.put("source_id", sourceId: Double)
        entry.put("title", s"$sourceId-$j entry title")
        entry.put("description", s"$sourceId-$j entry description")
        db.insert("entries", null, entry)
      }
    }
  }
}

