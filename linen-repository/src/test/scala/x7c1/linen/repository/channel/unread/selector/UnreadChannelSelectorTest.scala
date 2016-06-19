package x7c1.linen.repository.channel.unread.selector

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.{RobolectricTestRunner, RuntimeEnvironment}
import org.scalatest.junit.JUnitSuiteLike
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.database.mixin.UnreadChannelRecord
import x7c1.linen.database.struct.{ChannelStatusKey, SourceParts}
import x7c1.linen.repository.channel.my.ChannelCreator.InputToCreate
import x7c1.linen.repository.channel.my.{ChannelCreator, MyChannelConnection}
import x7c1.linen.repository.channel.subscribe.SubscribedChannel
import x7c1.linen.repository.channel.unread.UnreadChannel
import x7c1.linen.repository.date.Date
import x7c1.linen.repository.dummy.DummyEntryBinder
import x7c1.linen.repository.entry.EntryUrl
import x7c1.linen.repository.entry.unread.{EntryAccessor, EntrySourcePositionsFactory}
import x7c1.linen.repository.loader.crawling.LoadedEntry
import x7c1.linen.repository.source.setting.SampleFactory
import x7c1.linen.repository.unread.{AccessorLoader, BrowsedEntriesMarker}
import x7c1.linen.testing.{AllowTraversingAll, LogSetting}
import x7c1.wheat.modern.database.QueryExplainer

@Config(manifest=Config.NONE)
@RunWith(classOf[RobolectricTestRunner])
class UnreadChannelSelectorTest extends JUnitSuiteLike with AllowTraversingAll with LogSetting {
  import x7c1.linen.repository.dummy.DummySourceLoader.Implicits._

  import concurrent.duration._

  @Test
  def testTraverseOn(): Unit = {

    val context = RuntimeEnvironment.application
    val helper = new DatabaseHelper(context)
    val factory = new SampleFactory(helper)
    val account = factory.createAccount()

    val channelId1 = locally {// create first channel
      val Right(channelId) = ChannelCreator(helper, account) createChannel InputToCreate(
        channelName = "foo1"
      )
      createSources(channelId, sources = 5, entries = 4) foreach { sourceId =>
        val connection = new MyChannelConnection(helper.writable, sourceId)
        connection attachTo Seq(channelId)
      }
      channelId
    }
    val channelId2 = locally {// create second channel
      val Right(channelId) = ChannelCreator(helper, account) createChannel InputToCreate(
        channelName = "foo2"
      )
      createSources(channelId, sources = 5, entries = 4) foreach { sourceId =>
        val connection = new MyChannelConnection(helper.writable, sourceId)
        connection attachTo Seq(channelId)
      }
      channelId
    }
    locally {// before browsing
      val Right(all) = helper.selectorOf[SubscribedChannel].traverseOn(account)
      assertEquals(2, all.length)
      assertEquals(1, all.filter(_.name == "foo1").length)
      assertEquals(1, all.filter(_.name == "foo2").length)

      val Right(channels) = helper.selectorOf[UnreadChannel].traverseOn(account)
      assertEquals(2, channels.length)
      assertEquals(1, channels.filter(_.name == "foo1").length)
      assertEquals(1, channels.filter(_.name == "foo2").length)
    }
    def createOutlineSequence(channelId: Long) = {
      val sourceSequence = AccessorLoader.inspectSourceAccessor(
        db = helper.getReadableDatabase,
        accountId = account.accountId,
        channelId = channelId ).right.get

      val outlineSequence = {
        val db = helper.getReadableDatabase
        val sources = sourceSequence.sources
        val positions = new EntrySourcePositionsFactory(db).create(sources)
        EntryAccessor.forEntryOutline(db, sources, positions)
      }
      outlineSequence
    }
    def browse(channelId: Long, position: Int) = {
      val outlineSequence = createOutlineSequence(channelId)
      val marker = new BrowsedEntriesMarker(helper, outlineSequence)
      marker.touchOutlinePosition(position)
      marker.markAsRead()
    }
    /* dump
    createOutlineSequence(channelId1).toSeq foreach {
      case s: SourceHeadlineContent =>
        println(s.title)
      case s: EntryContent[UnreadOutline] =>
        println(s)
    }
    // */
    locally {// after entries of 4 sources are browsed
      browse(channelId1, (4 + 1) * 4)
      val Right(channels) = helper.selectorOf[UnreadChannel].traverseOn(account)
      assertEquals(2, channels.length)
    }
    locally {// after an entry of last source is browsed (= channelId1 is marked as read)
      browse(channelId1, 1)
      val Right(channels) = helper.selectorOf[UnreadChannel].traverseOn(account)
      assertEquals(1, channels.length)
    }
    locally {// after channelId2 is also marked as read
      browse(channelId2, 21)
      val Right(channels) = helper.selectorOf[UnreadChannel].traverseOn(account)
      assertEquals(0, channels.length)
    }
  }

  @Test
  def testQueryPlanForTraverseOn(): Unit = {
    val context = RuntimeEnvironment.application
    val helper = new DatabaseHelper(context)
    val factory = new SampleFactory(helper)
    val account = factory.createAccount()

    val channelId1 = locally {// create first channel
      val Right(channelId) = ChannelCreator(helper, account) createChannel InputToCreate(
        channelName = "foo1"
      )
      createSources(channelId, sources = 5, entries = 4) foreach { sourceId =>
        val connection = new MyChannelConnection(helper.writable, sourceId)
        connection attachTo Seq(channelId)
      }
      channelId
    }
    locally {// create second channel
      val Right(channelId) = ChannelCreator(helper, account) createChannel InputToCreate(
        channelName = "foo2"
      )
      createSources(channelId, sources = 5, entries = 4) foreach { sourceId =>
        val connection = new MyChannelConnection(helper.writable, sourceId)
        connection attachTo Seq(channelId)
      }
      channelId
    }
    locally {// before browsing
      val Right(all) = helper.selectorOf[SubscribedChannel].traverseOn(account)
      assertEquals(2, all.length)
      assertEquals(1, all.filter(_.name == "foo1").length)
      assertEquals(1, all.filter(_.name == "foo2").length)

      val Right(channels) = helper.selectorOf[UnreadChannel].traverseOn(account)
      assertEquals(2, channels.length)
      assertEquals(1, channels.filter(_.name == "foo1").length)
      assertEquals(1, channels.filter(_.name == "foo2").length)
    }
    val query = UnreadChannelRecord.toDetect queryAbout ChannelStatusKey(
      channelId = channelId1,
      accountId = account.accountId
    )
    val plans = QueryExplainer(helper.getReadableDatabase) explain query
    assertEquals(
      "USE TEMP B-TREE",
      false, plans.exists(_.useTempBtree)
    )
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
  def createSources(channelId: Long, sources: Int, entries: Int) = {
    val context = RuntimeEnvironment.application
    val helper = new DatabaseHelper(context)

    val partsList = (1 to sources) map { n =>
      SourceParts(
        title = s"title $n",
        url = s"http://example.com/ch-$channelId/sources/$n",
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