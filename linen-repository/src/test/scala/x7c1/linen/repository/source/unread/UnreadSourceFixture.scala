package x7c1.linen.repository.source.unread

import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.database.struct.EntryParts
import x7c1.linen.repository.date.Date
import x7c1.linen.repository.dummy.{DummyEntryBinder, DummySourceLoader}
import x7c1.linen.repository.entry.EntryUrl
import x7c1.linen.repository.loader.crawling.LoadedEntry
import x7c1.linen.repository.source.setting.{ChannelOwner, ChannelSourceParts, SampleFactory}
import x7c1.wheat.modern.database.WritableDatabase

class UnreadSourceFixture(helper: DatabaseHelper) {
  import DummySourceLoader.Implicits._

  import concurrent.duration._

  private val db = helper.getWritableDatabase
  private val factory = new SampleFactory(helper)

  val account1 = factory.createAccount()
  val account2 = factory.createAccount()
  val channel1 = factory.createChannel(account1)
  val channel2 = factory.createChannel(account2)

  val channelOwner1 = new ChannelOwner(db, channel1.channelId, account1.accountId)
  val Right(sourceId1) = channelOwner1 addSource ChannelSourceParts(
    url = "http://example.com/1",
    title = "title1",
    description = "description1",
    rating = 11
  )
  val Right(sourceId2) = channelOwner1 addSource ChannelSourceParts(
    url = "http://example.com/2",
    title = "title2",
    description = "description2",
    rating = 33
  )
  val Right(sourceWithoutEntry) = channelOwner1 addSource ChannelSourceParts(
    url = "http://example.com/3",
    title = "title3",
    description = "description3",
    rating = 55
  )
  DummyEntryBinder(helper).bind(sourceId1, Seq(
    LoadedEntry(
      title = "sample-entry1-1",
      content = "sample-content",
      author = "sample-author",
      url = EntryUrl("http://sample-url"),
      createdAt = Date.current() - 1.day
    ),
    LoadedEntry(
      title = "sample-entry1-2",
      content = "sample-content2",
      author = "sample-author1-2",
      url = EntryUrl("http://sample-url2"),
      createdAt = Date.current()
    )
  ))
  DummyEntryBinder(helper).bind(sourceId2, Seq(
    LoadedEntry(
      title = "sample-entry2-1",
      content = "sample-content2-1",
      author = "sample-author2-1",
      url = EntryUrl("http://sample-url2-1"),
      createdAt = Date.current()
    )
  ))
}

class SourceEntryUpdater(db: SQLiteDatabase, sourceId: Long){
  def addEntry(entry: LoadedEntry): Either[SQLException, Long] = {
    val parts = EntryParts(
      sourceId = sourceId,
      title = entry.title,
      content = entry.content,
      author = entry.author,
      url = entry.url,
      createdAt = entry.createdAt
    )
    new WritableDatabase(db).insert(parts)
  }
}
