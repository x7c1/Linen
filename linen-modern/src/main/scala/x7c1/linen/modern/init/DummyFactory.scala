package x7c1.linen.modern.init

import android.app.Activity
import android.content.{ContentValues, Context}
import android.widget.Toast
import x7c1.linen.glue.res.layout.MainLayout
import x7c1.linen.modern.accessor.DummyString.words
import x7c1.linen.modern.accessor.{AccountAccessor, AccountParts, AccountsTable, LinenOpenHelper}
import x7c1.linen.modern.struct.Date
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.Imports._
import x7c1.wheat.modern.patch.TaskAsync.async

object DummyFactory {
  def setup(layout: MainLayout, activity: Activity) = {
    layout.createDummies onClick { _ =>
      async {
        createDummies(activity)(100)
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

    val accountId1 = AccountAccessor.create(db) findAt 0 map (_.accountId) getOrElse {
      val Right(id) = AccountsTable(db) insert AccountParts(
        nickname = s"sample-user-1",
        biography = s"sample-biography-1",
        createdAt = Date.current()
      )
      id
    }
    val accountId2 = AccountAccessor.create(db) findAt 1 map (_.accountId) getOrElse {
      val Right(id) = AccountsTable(db) insert AccountParts(
        nickname = s"sample-user-2",
        biography = s"sample-biography-2",
        createdAt = Date.current()
      )
      id
    }
    val channelId = {
      val cursor = db.rawQuery("SELECT * FROM channels ORDER BY _id LIMIT 1", Array())
      if (cursor.getCount > 0){
        cursor.moveToFirst()
        val idIndex = cursor getColumnIndex "_id"
        cursor getInt idIndex
      } else {
        val item = new ContentValues()
        item.put("name", s"sample channel name1")
        item.put("description", s"sample channel description1")
        item.put("account_id", accountId1: Double)
        item.put("created_at", Date.timestamp: Double)
        db.insertOrThrow("channels", null, item)

        val item2 = new ContentValues()
        item2.put("name", s"sample channel name2")
        item2.put("description", s"sample channel description2")
        item2.put("account_id", accountId1: Double)
        item2.put("created_at", Date.timestamp: Double)
        db.insertOrThrow("channels", null, item2)
      }
    }
    (1 to n) foreach { i =>
      val source = new ContentValues()
      source.put("title", s"$i-title")
      source.put("url", s"http://example.com/source$i")
      source.put("description", s"description-$i " + words(1,15))
      source.put("created_at", Date.timestamp: Double)
      val sourceId = db.insertOrThrow("sources", null, source)

      val status1 = new ContentValues()
      status1.put("source_id", sourceId: Double)
      status1.put("account_id", accountId1: Double)
      status1.put("created_at", Date.timestamp: Double)
      db.insertOrThrow("source_statuses", null, status1)

      val rating1 = new ContentValues()
      rating1.put("source_id", sourceId: Double)
      rating1.put("owner_account_id", accountId1: Double)
      rating1.put("rating", sourceId: Double)
      rating1.put("created_at", Date.timestamp: Double)
      db.insertOrThrow("source_ratings", null, rating1)

      val status2 = new ContentValues()
      status2.put("source_id", sourceId: Double)
      status2.put("account_id", accountId2: Double)
      status2.put("created_at", Date.timestamp: Double)
      db.insertOrThrow("source_statuses", null, status2)

      val rating2 = new ContentValues()
      rating2.put("source_id", sourceId: Double)
      rating2.put("owner_account_id", accountId2: Double)
      rating2.put("rating", sourceId: Double)
      rating2.put("created_at", Date.timestamp: Double)
      db.insertOrThrow("source_ratings", null, rating2)

      val channelSourceMap = new ContentValues()
      channelSourceMap.put("channel_id", channelId: Double)
      channelSourceMap.put("source_id", sourceId: Double)
      channelSourceMap.put("created_at", Date.timestamp: Double)
      db.insertOrThrow("channel_source_map", null, channelSourceMap)

      if (i % 10 == 0){
        Log info s"source at $i inserted"
      }
      (1 to 10) foreach { j =>
        val entry = new ContentValues()
        entry.put("source_id", sourceId: Double)
        entry.put("title", s"$sourceId-$j entry title")
        entry.put("content", s"$sourceId-$j entry content " + words(100,500))
        entry.put("url", s"http://example.com/entry-$j")
        entry.put("created_at", Date.timestamp: Double)
        val entryId = db.insertOrThrow("entries", null, entry)

        if (i == 3){
          val status1 = new ContentValues()
          status1.put("start_entry_id", entryId: Double)
          db.update(
            "source_statuses", status1,
            "source_id = ? AND account_id = ?",
            Array(sourceId.toString, accountId1.toString)
          )
          val status2 = new ContentValues()
          status2.put("start_entry_id", entryId: Double)
          db.update(
            "source_statuses", status2,
            "source_id = ? AND account_id = ?",
            Array(sourceId.toString, accountId2.toString)
          )
        }
        if (i == 4){
          val status2 = new ContentValues()
          status2.put("start_entry_id", entryId: Double)
          db.update(
            "source_statuses", status2,
            "source_id = ? AND account_id = ?",
            Array(sourceId.toString, accountId2.toString)
          )
        }

      }
    }
  }
}
