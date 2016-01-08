package x7c1.linen.modern.init

import android.app.Activity
import android.content.{ContentValues, Context}
import android.widget.Toast
import x7c1.linen.glue.res.layout.MainLayout
import x7c1.linen.modern.accessor.DummyString.words
import x7c1.linen.modern.accessor.{EntryParts, ChannelSourceMapParts, SourceRatingParts, SourceStatusParts, SourceParts, AccountAccessor, AccountParts, ChannelAccessor, ChannelParts, LinenOpenHelper}
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
    val writable = helper.writableDatabase

    val accountId1 = AccountAccessor.create(db) findAt 0 map (_.accountId) getOrElse {
      val Right(id) = writable insert AccountParts(
        nickname = s"sample-user-1",
        biography = s"sample-biography-1",
        createdAt = Date.current()
      )
      id
    }
    val accountId2 = AccountAccessor.create(db) findAt 1 map (_.accountId) getOrElse {
      val Right(id) = writable insert AccountParts(
        nickname = s"sample-user-2",
        biography = s"sample-biography-2",
        createdAt = Date.current()
      )
      id
    }
    val channelAccessor = ChannelAccessor.create(db, accountId1)
    val channelId = channelAccessor findAt 0 map (_.channelId) getOrElse {
      writable insert ChannelParts(
        accountId = accountId1,
        name = s"sample channel name1",
        description = s"sample channel description1",
        createdAt = Date.current()
      )
      val Right(id) = writable insert ChannelParts(
        accountId = accountId1,
        name = s"sample channel name2",
        description = s"sample channel description2",
        createdAt = Date.current()
      )
      id
    }
    (1 to n) foreach { i =>
      val Right(sourceId) = writable insert SourceParts(
        title = s"$i-title",
        url = s"http://example.com/source$i",
        description = s"description-$i " + words(1,15),
        createdAt = Date.current()
      )
      writable insert SourceStatusParts(
        sourceId = sourceId,
        accountId = accountId1,
        createdAt = Date.current()
      )
      writable insert SourceRatingParts(
        sourceId = sourceId,
        ownerAccountId = accountId1,
        rating = sourceId.toInt,
        createdAt = Date.current()
      )
      writable insert SourceStatusParts(
        sourceId = sourceId,
        accountId = accountId2,
        createdAt = Date.current()
      )
      writable insert SourceRatingParts(
        sourceId = sourceId,
        ownerAccountId = accountId2,
        rating = sourceId.toInt,
        createdAt = Date.current()
      )
      writable insert ChannelSourceMapParts(
        channelId = channelId,
        sourceId = sourceId,
        createdAt = Date.current()
      )
      if (i % 10 == 0){
        Log info s"source at $i inserted"
      }
      (1 to 10) foreach { j =>
        val Right(entryId) = writable insert EntryParts(
          sourceId = sourceId,
          title = s"$sourceId-$j entry title",
          content = s"$sourceId-$j entry content " + words(100,500),
          url = s"http://example.com/entry-$j",
          createdAt = Date.current()
        )
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
