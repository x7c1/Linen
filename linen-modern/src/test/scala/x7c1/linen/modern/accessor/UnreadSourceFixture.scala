package x7c1.linen.modern.accessor

import x7c1.linen.modern.struct.Date

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
    rating = 33
  )
  val Right(sourceId2) = channelOwner1 addSource ChannelSourceParts(
    url = "http://example.com/2",
    title = "title2",
    description = "description2",
    rating = 11
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
    url = "sample-url",
    createdAt = Date.current()
  )
  val Right(entryId1_2) = new SourceEntryUpdater(db, sourceId1) addEntry RetrievedEntry(
    title = "sample-entry1-2",
    content = "sample-content2",
    url = "sample-url2",
    createdAt = Date.current()
  )
  val Right(entryId2_1) = new SourceEntryUpdater(db, sourceId2) addEntry RetrievedEntry(
    title = "sample-entry2-1",
    content = "sample-content2-1",
    url = "sample-url2-1",
    createdAt = Date.current()
  )
}