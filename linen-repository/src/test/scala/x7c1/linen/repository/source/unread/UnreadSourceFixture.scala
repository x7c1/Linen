package x7c1.linen.repository.source.unread

import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import x7c1.linen.database.control.LinenOpenHelper
import x7c1.linen.database.struct.EntryParts
import x7c1.linen.repository.date.Date
import x7c1.linen.repository.entry.EntryUrl
import x7c1.linen.repository.source.setting.{ChannelSourceParts, ChannelOwner, SampleFactory}
import x7c1.wheat.modern.database.WritableDatabase

class UnreadSourceFixture(helper: LinenOpenHelper) {
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
  val Right(entryId1_1) = new SourceEntryUpdater(db, sourceId1) addEntry RetrievedEntry(
    title = "sample-entry1-1",
    content = "sample-content",
    url = EntryUrl("http://sample-url"),
    createdAt = Date.current()
  )
  val Right(entryId1_2) = new SourceEntryUpdater(db, sourceId1) addEntry RetrievedEntry(
    title = "sample-entry1-2",
    content = "sample-content2",
    url = EntryUrl("http://sample-url2"),
    createdAt = Date.current()
  )
  val Right(entryId2_1) = new SourceEntryUpdater(db, sourceId2) addEntry RetrievedEntry(
    title = "sample-entry2-1",
    content = "sample-content2-1",
    url = EntryUrl("http://sample-url2-1"),
    createdAt = Date.current()
  )
}

case class RetrievedEntry(
  title: String,
  content: String,
  url: EntryUrl,
  createdAt: Date
)

class SourceEntryUpdater(db: SQLiteDatabase, sourceId: Long){
  def addEntry(entry: RetrievedEntry): Either[SQLException, Long] = {
    val parts = EntryParts(
      sourceId = sourceId,
      title = entry.title,
      content = entry.content,
      url = entry.url,
      createdAt = entry.createdAt
    )
    new WritableDatabase(db).insert(parts)
  }
}
