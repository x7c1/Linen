package x7c1.linen.repository.unread

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.{RobolectricTestRunner, RuntimeEnvironment}
import org.scalatest.junit.JUnitSuiteLike
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.database.struct.source_statuses.Key
import x7c1.linen.database.struct.{EntryRecord, SourceParts, retrieved_source_marks, source_statuses}
import x7c1.linen.repository.channel.my.MyChannelConnection
import x7c1.linen.repository.crawler.LoadedEntry
import x7c1.linen.repository.date.Date
import x7c1.linen.repository.dummy.DummyEntryBinder
import x7c1.linen.repository.entry.EntryUrl
import x7c1.linen.repository.entry.unread.{EntryAccessor, EntrySourcePositionsFactory}
import x7c1.linen.repository.source.setting.SampleFactory
import x7c1.linen.repository.source.unread.{UnreadSource, UnreadSourceRow}
import x7c1.linen.testing.LogSetting
import x7c1.wheat.modern.either.OptionRight

@Config(manifest=Config.NONE)
@RunWith(classOf[RobolectricTestRunner])
class BrowsedEntriesMarkerTest extends JUnitSuiteLike with LogSetting {
  import x7c1.linen.repository.dummy.DummySourceLoader.Implicits._

  import concurrent.duration._

//  ShadowLog.stream = new PrintStream(new FileOutputStream("hoge.log"), true)

  @Test
  def testMarkAsRead(): Unit = {

//    ShadowLog.stream = System.out

//    ShadowLog.setLoggable("x7c1", Log.VERBOSE)
//    ShadowLog.setupLogging()

    val context = RuntimeEnvironment.application
    val helper = new DatabaseHelper(context)
    val factory = new SampleFactory(helper)
    val account = factory.createAccount()
    val channel = factory.createChannel(account)

    createSources(sources = 5, entries = 4) foreach { sourceId =>
      val connection = new MyChannelConnection(helper.writable, sourceId)
      connection attachTo Seq(channel.channelId)
    }
    val sourceAccessor = AccessorLoader.inspectSourceAccessor(
      db = helper.getReadableDatabase,
      accountId = account.accountId,
      channelId = channel.channelId ).right.get

    assertEquals(5, sourceAccessor.length)

    val outlineAccessor = {
      val db = helper.getReadableDatabase
      val sources = sourceAccessor.sources
      val positions = new EntrySourcePositionsFactory(db).create(sources)
      EntryAccessor.forEntryOutline(db, sources, positions)
    }
    assertEquals((4 + 1) * 5, outlineAccessor.length)

    val marker = new BrowsedEntriesMarker(helper, outlineAccessor)
    marker.touchOutlinePosition(12)
    marker.markAsRead()

    val source0 = sourceAccessor sources 0

    // confirm first source is marked as read
    {
      val OptionRight(Some(status0)) = helper.readable.find[source_statuses] by Key(
        accountId = account.accountId,
        sourceId = source0.id
      )
      val Some(latest0) = outlineAccessor.latestEntriesTo(12) collectFirst {
        case entry if entry.sourceId == source0.id => entry
      }
      assertEquals("title 5", source0.title)
      assertEquals(
        latest0.entryId -> latest0.createdAt.timestamp,
        status0.start_entry_id -> status0.start_entry_created_at
      )
    }
    val source1 = sourceAccessor sources 1

    // confirm second source is marked as read
    {
      val OptionRight(Some(status1)) = helper.readable.find[source_statuses] by Key(
        accountId = account.accountId,
        sourceId = source1.id
      )
      val Some(latest1) = outlineAccessor.latestEntriesTo(12) collectFirst {
        case entry if entry.sourceId == source1.id => entry
      }
      assertEquals("title 4", source1.title)
      assertEquals(
        latest1.entryId -> latest1.createdAt.timestamp,
        status1.start_entry_id -> status1.start_entry_created_at
      )
    }

    // SourceAccessor should NOT include source0 nor source1
    {
      val updatedSourceAccessor = AccessorLoader.inspectSourceAccessor(
        db = helper.getReadableDatabase,
        accountId = account.accountId,
        channelId = channel.channelId ).right.get

      assertEquals(2, updatedSourceAccessor.length)

      val Some(UnreadSourceRow(s2: UnreadSource)) = updatedSourceAccessor findAt 0
      assertEquals("title 2", s2.title)

      val Some(UnreadSourceRow(s1: UnreadSource)) = updatedSourceAccessor findAt 1
      assertEquals("title 1", s1.title)

      val s = updatedSourceAccessor findAt 2
      assertEquals(None, s)
    }

    // load new entries of source0
    {
      DummyEntryBinder(helper).bind(source0.id, (1 to 3) map { n =>
        LoadedEntry(
          title = s"new title $n",
          content = s"new content $n",
          author = s"new author $n",
          url = EntryUrl(s"http://example.com/new-entry/$n"),
          createdAt = Date.current() + (10 + n).day
        )
      })
      val s = helper.selectorOf[EntryRecord] collectFrom source0
      println(s)
//      println(s.selector[Seq[EntryRecord]].hoge)
//      println(s.getClass)
//      println(s.hoge)

//      val Right(entries2) = helper.readable.select2[Seq[EntryRecord]].by(source0)

      val Right(entries) = helper.selectorOf[EntryRecord] collectFrom source0
      assertEquals(7, entries.length)

      println("?????")
      entries foreach { entry =>
        println("===")
        println(entry.entry_id)
        println(entry.title)
        println(entry.created_at.typed.format)
      }
    }

    val OptionRight(Some(xx)) = helper.readable.find[retrieved_source_marks] by source0.id
    println("----")
    println(xx)
    println(xx.latest_entry_id)
    println(xx.latest_entry_created_at.typed.format)


    // SourceAccessor should include source0
    {
      val updatedSourceAccessor = AccessorLoader.inspectSourceAccessor(
        db = helper.getReadableDatabase,
        accountId = account.accountId,
        channelId = channel.channelId ).right.get

      println("new!")
      assertEquals(3, updatedSourceAccessor.length)
    }


    //todo:
    // add new entries to first source
    // test SourceAccessor which should include source1

    val sourceRow = sourceAccessor.findAt(0)
    println(s"${sourceAccessor.length}")
    println(sourceRow)


  }
  def createDummyEntries(sourceId: Long, count: Int) = (1 to count) map { n =>
    LoadedEntry(
      title = s"sample entry $n",
      content = s"sample content $n",
      author = s"sample author $n",
      url = EntryUrl(s"http://example.com/entry/$sourceId-$n"),
      createdAt = Date.current() + n.day
    )
  }
  def createSources(sources: Int, entries: Int) = {
    val context = RuntimeEnvironment.application
    val helper = new DatabaseHelper(context)

    val partsList = (1 to sources) map { n =>
      SourceParts(
        title = s"title $n",
        url = s"http://example.com/sources/$n",
        description = s"sample description $n",
        createdAt = Date.current()
      )
    }
    val sourceIds = partsList map { parts =>
      val Right(sourceId) = helper.writable insert parts
      sourceId
    }
    sourceIds foreach { id =>
      DummyEntryBinder(helper).bind(id, createDummyEntries(id, count = entries))
    }
    sourceIds
  }

}