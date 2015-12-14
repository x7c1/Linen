package x7c1.linen.modern.init

import android.app.Activity
import android.content.{ContentValues, Context}
import android.widget.Toast
import x7c1.linen.glue.res.layout.MainLayout
import x7c1.linen.modern.accessor.DummyString.words
import x7c1.linen.modern.accessor.LinenOpenHelper
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.Imports._
import x7c1.wheat.modern.patch.TaskAsync.async

object DummyFactory {
  def setup(layout: MainLayout, activity: Activity) = {
    layout.createDummies onClick { _ =>
      async {
        createDummies(activity)(500)
        layout.createDummies runUi { _ =>
          Toast.makeText(activity, "dummies inserted", Toast.LENGTH_SHORT).show()
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
      source.put("title", s"$i-title")
      source.put("description", s"description-$i " + words(1,15))
      source.put("rating", i * 10 : Double)
      val sourceId = db.insert("sources", null, source)

      val listId = 123
      val listSourceMap = new ContentValues()
      listSourceMap.put("list_id", listId: Double)
      listSourceMap.put("source_id", sourceId: Double)
      db.insert("list_source_map", null, listSourceMap)

      if (i % 10 == 0){
        Log info s"source at $i inserted"
      }
      (1 to 10) foreach { j =>
        val entry = new ContentValues()
        entry.put("source_id", sourceId: Double)
        entry.put("title", s"$sourceId-$j entry title")
        entry.put("content", s"$sourceId-$j entry content " + words(100,500))
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
