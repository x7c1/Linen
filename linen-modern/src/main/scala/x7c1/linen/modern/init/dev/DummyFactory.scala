package x7c1.linen.modern.init.dev

import android.content.Context
import x7c1.linen.modern.accessor.DummyString.words
import x7c1.linen.modern.accessor.{AccountAccessor, AccountParts, ChannelAccessor, ChannelParts, ChannelSourceMapParts, EntryParts, LinenOpenHelper, SourceParts, SourceRatingParts, SourceStatusAsStarted, SourceStatusParts}
import x7c1.linen.modern.struct.Date
import x7c1.wheat.macros.logger.Log

import scala.util.Random

object DummyFactory {
  def createDummies(context: Context)(n: Int): Unit = {
    createDummies0(context)(n)(_ => ())
  }
  def createDummies0(context: Context)(n: Int)(callback: Int => Unit): Unit = {
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
      writable update SourceTitle(
        sourceId = sourceId,
        title = s"$sourceId-title"
      )
      writable insert SourceStatusParts(
        sourceId = sourceId,
        accountId = accountId1,
        createdAt = Date.current()
      )
      writable insert SourceRatingParts(
        sourceId = sourceId,
        ownerAccountId = accountId1,
        rating = generateRating(),
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
        rating = generateRating(),
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
          writable update SourceStatusAsStarted(
            startEntryId = entryId,
            sourceId = sourceId,
            accountId = accountId1
          )
          writable update SourceStatusAsStarted(
            startEntryId = entryId,
            sourceId = sourceId,
            accountId = accountId2
          )
        }
        if (i == 4){
          writable update SourceStatusAsStarted(
            startEntryId = entryId,
            sourceId = sourceId,
            accountId = accountId2
          )
        }
      }
      callback(i)
    }
  }
  def generateRating() = {
    val range = 1 to 100
    range(Random.nextInt(range.size - 1))
  }
}
