package x7c1.linen.repository.source.unread

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.{RobolectricTestRunner, RuntimeEnvironment}
import org.scalatest.junit.JUnitSuiteLike
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.database.struct.EntryRecord
import x7c1.linen.repository.account.dev.AccountAccessor
import x7c1.linen.repository.channel.my.{MyChannel, MyChannelAccessor}
import x7c1.linen.repository.dummy.DummyFactory
import x7c1.linen.repository.entry.unread.{EntryAccessor, EntryContent, EntrySourcePositions, EntrySourcePositionsFactory}
import x7c1.linen.repository.unread.{AccessorLoader, EntryKind, SourceKind}
import x7c1.wheat.modern.database.QueryExplainer

import scala.util.Success

@Config(manifest=Config.NONE)
@RunWith(classOf[RobolectricTestRunner])
class SourceOpenHelperTest extends JUnitSuiteLike {

  def isSortedDesc(xs: Seq[Long]) = {
    xs.zip(xs.tail).forall(x => x._1 > x._2)
  }
  @Test
  def testQueryForSourceArea() = {
    val context = RuntimeEnvironment.application
    val helper = new DatabaseHelper(context)
    val fixture = new UnreadSourceFixture(helper)

    val db = helper.getWritableDatabase
    val Success(accessor) = UnreadSourceAccessor.create(
      db,
      fixture.account1 -> fixture.channel1
    )
    val sources = 0 until accessor.length flatMap accessor.findAt collect {
      case x: UnreadSource => x
    }
    assertEquals(Seq(33, 11), sources.map(_.rating))
    assertEquals(Seq("description2", "description1"), sources.map(_.description))

    val latestEntries = sources.map(_.latestEntryId).flatMap { entryId =>
      helper.selectorOf[EntryRecord].findBy(entryId).toOption
    }
    assertEquals(
      Seq("sample-entry2-1", "sample-entry1-2"),
      latestEntries.map(_.title)
    )
  }
  @Test
  def testQueryPlanForSourceArea() = {
    val context = RuntimeEnvironment.application
    val helper = new DatabaseHelper(context)
    val fixture = new UnreadSourceFixture(helper)

    val db = helper.getWritableDatabase
    val plans = QueryExplainer(db).
      explain(
        UnreadSourceAccessor.createQuery(
          fixture.channel1.channelId,
          fixture.account1.accountId
        ))

//    plans foreach println
    assertEquals("USE TEMP B-TREE",
      false, plans.exists(_.useTempBtree))
  }

  @Test
  def testQueryForEntryArea() = {
    val context = RuntimeEnvironment.application
    DummyFactory.createDummies(context)(5)

    val helper = new DatabaseHelper(context)
    val db = helper.getReadableDatabase
    val Some((accountId, sourceAccessor)) = inspectSourceAccessor(helper)
    val sources = sourceAccessor.sources
    val positions = {
      val factory = new EntrySourcePositionsFactory(db)
      factory create sources
    }
    val accessor = EntryAccessor.forEntryOutline(db, sources, positions)
    val entries = 0 until accessor.length flatMap accessor.findAt

    assertEquals(true, entries.exists {
      case EntryContent(entry) =>
        entry.shortTitle == "5-1 entry title"
      case _ =>
        false
    })
    assertEquals(false, entries.exists {
      case EntryContent(entry) =>
        entry.shortTitle == "3-1 entry title"
      case _ =>
        false
    })
  }
  @Test
  def testQueryPlanForEntryPosition() = {
    val context = RuntimeEnvironment.application
    DummyFactory.createDummies(context)(5)

    val helper = new DatabaseHelper(context)
    val db = helper.getReadableDatabase
    val Some((accountId, sourceAccessor)) = inspectSourceAccessor(helper)
    val sources = sourceAccessor.sources
    val query = EntrySourcePositions.createQuery(sources)
    val plans = QueryExplainer(db).explain(query)

//    println(query.sql)
//    plans foreach println

    assertEquals("USE TEMP B-TREE",
      false, plans.exists(_.useTempBtree))
  }

  @Test
  def testUnreadRowKind() = {

    val context = RuntimeEnvironment.application
    DummyFactory.createDummies(context)(5)

    val helper = new DatabaseHelper(context)
    val db = helper.getReadableDatabase
    val Some((accountId, sourceAccessor)) = inspectSourceAccessor(helper)
    val sources = sourceAccessor.sources
    val positions = {
      val factory = new EntrySourcePositionsFactory(db)
      factory create sources
    }
    val accessor = EntryAccessor.forEntryOutline(db, sources, positions)

    assertEquals(Some(SourceKind), accessor.findKindAt(0))
    assertEquals(Some(EntryKind), accessor.findKindAt(1))
  }
  private def inspectSourceAccessor(helper: DatabaseHelper) = {
    val db = helper.getReadableDatabase
    for {
      accountId <- AccountAccessor.findCurrentAccountId(db)
      channel <- MyChannelAccessor.createForDebug(helper, accountId).findAt(0).collect {
        case x: MyChannel => x
      }
      either = AccessorLoader.inspectSourceAccessor(db, accountId -> channel)
      accessor <- either.right.toOption
    } yield accountId -> accessor
  }
}



