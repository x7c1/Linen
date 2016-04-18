package x7c1.linen.repository.dummy

import android.content.Context
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.database.struct.{SourceStatusAsStarted, SourceStatusParts, SourceParts, SourceRatingParts, EntryParts, ChannelParts, ChannelSourceMapParts, AccountParts}
import x7c1.linen.repository.account.dev.AccountAccessor
import x7c1.linen.repository.channel.my.{MyChannel, MyChannelAccessor}
import x7c1.linen.repository.date.Date
import x7c1.linen.repository.dummy.DummyString.words
import x7c1.linen.repository.entry.EntryUrl
import x7c1.linen.repository.source.unread.SourceTitle
import x7c1.wheat.macros.logger.Log

import scala.util.Random

object DummyFactory {
  def createDummies(context: Context)(n: Int): Unit = {
    createDummies0(context)(n)(_ => ())
  }
  def createDummies0(context: Context)(n: Int)(callback: Int => Unit): Unit = {
    val helper = new DatabaseHelper(context)
    val db = helper.getWritableDatabase
    val writable = helper.writable

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
    val channelAccessor = MyChannelAccessor.createForDebug(db, accountId1)
    val row = channelAccessor findAt 0 collect { case x: MyChannel => x }
    val channelId = row map (_.channelId) getOrElse {
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
    val timestamp = Date.timestamp
    (1 to n) foreach { i =>
      val Right(sourceId) = writable insert SourceParts(
        title = s"$i-title",
        url = s"http://example.com/source-$i/$timestamp",
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
        accountId = accountId1,
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
        accountId = accountId2,
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
        val entryCreatedAt = Date.current()
        val Right(entryId) = writable insert EntryParts(
          sourceId = sourceId,
          title = s"$sourceId-$j entry title",
          content = s"$sourceId-$j entry content " + words(100,500),
          author = s"author $sourceId-$j",
          url = EntryUrl(s"http://example.com/entry-$sourceId-$j"),
          createdAt = Date.current()
        )
        if (i == 3){
          writable update SourceStatusAsStarted(
            startEntryId = entryId,
            startEntryCreatedAt = entryCreatedAt.timestamp,
            sourceId = sourceId,
            accountId = accountId1
          )
          writable update SourceStatusAsStarted(
            startEntryId = entryId,
            startEntryCreatedAt = entryCreatedAt.timestamp,
            sourceId = sourceId,
            accountId = accountId2
          )
        }
        if (i == 4){
          writable update SourceStatusAsStarted(
            startEntryId = entryId,
            startEntryCreatedAt = entryCreatedAt.timestamp,
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
