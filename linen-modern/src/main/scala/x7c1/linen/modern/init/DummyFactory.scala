package x7c1.linen.modern.init

import android.app.Activity
import android.content.{ContentValues, Context}
import android.widget.Toast
import x7c1.linen.glue.res.layout.MainLayout
import x7c1.linen.modern.accessor.{EntryProvider, LinenOpenHelper}
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
        createDummies(activity)(500)
        layout.createDummies runUi { _ =>
          Toast.makeText(activity, "dummies inserted", Toast.LENGTH_SHORT).show()
        }
      }
    }
    layout.showAllEntries onClick { _ =>
      async {
        val cursor = activity.getContentResolver.query(
          EntryProvider.ContentUri, null, null, null, null, null)

        activity.startManagingCursor(cursor)

        while(cursor.moveToNext()){
          (0 to cursor.getColumnCount - 1) foreach { i =>
            val column = cursor getColumnName i
            val value = cursor getString i
            Log error s"$column = $value"
          }
        }
      }
    }
    layout.initDummies onClick { _ =>
      activity deleteDatabase "linen-db"
      Toast.makeText(activity, "database deleted", Toast.LENGTH_SHORT).show()
    }

  }

  def createDummies(context: Context)(n: Int) = {
    val helper = new LinenOpenHelper(context)
    val db = helper.getWritableDatabase
    (1 to n) foreach { i =>
      val source = new ContentValues()
      source.put("title", s"title-$i")
      source.put("description", s"description-$i")
      val sourceId = db.insert("sources", null, source)

      val listId = 123
      val listSourceMap = new ContentValues()
      listSourceMap.put("list_id", listId: Double)
      listSourceMap.put("source_id", sourceId: Double)
      db.insert("list_source_map", null, listSourceMap)

      (1 to 10) foreach { j =>
        val entry = new ContentValues()
        entry.put("source_id", sourceId: Double)
        entry.put("title", s"$sourceId-$j entry title")
        entry.put("content", s"$sourceId-$j entry content")
        if (i == 3){
          entry.put("read_state", 1: Double)
        } else {
          entry.put("read_state", 0: Double)// unread
        }
        db.insert("entries", null, entry)
      }
    }
  }
}

