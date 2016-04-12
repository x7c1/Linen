package x7c1.linen.repository.source.unread

import android.database.sqlite.SQLiteDatabase
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.{RobolectricTestRunner, RuntimeEnvironment}
import org.scalatest.junit.JUnitSuiteLike
import x7c1.linen.database.{DebugTools, LinenOpenHelper, QueryExplainer}
import x7c1.linen.domain.{SourceKind, EntryKind}
import x7c1.linen.repository.channel.my.{MyChannel, MyChannelAccessor}
import x7c1.linen.repository.dev.{AccountAccessor, DummyFactory}
import x7c1.linen.repository.entry.unread.{EntryContent, UnreadEntryRow, EntryAccessor}
import x7c1.linen.repository.unread.AccessorLoader

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
    val helper = new LinenOpenHelper(context)
    val fixture = new UnreadSourceFixture(helper)

    val db = helper.getWritableDatabase
    val Success(accessor) = UnreadSourceAccessor.create(
      db,
      fixture.channel1.channelId,
      fixture.account1.accountId
    )
    val sources = (0 to accessor.length - 1).flatMap(accessor.findAt) collect {
      case UnreadSourceRow(x: UnreadSource) => x
    }
    assertEquals(Seq(33, 11), sources.map(_.rating))
    assertEquals(Seq("description2", "description1"), sources.map(_.description))
    assertEquals(
      Seq(fixture.entryId2_1, fixture.entryId1_2),
      sources.map(_.latestEntryId)
    )
  }
  @Test
  def testQueryPlanForSourceArea() = {
    val context = RuntimeEnvironment.application
    val helper = new LinenOpenHelper(context)
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
      false, plans.exists(_.detail contains "USE TEMP B-TREE"))
  }

  @Test
  def testQueryForEntryArea() = {
    val context = RuntimeEnvironment.application
    DummyFactory.createDummies(context)(5)

    val db = new LinenOpenHelper(context).getReadableDatabase
    val Some(sourceAccessor) = inspectSourceAccessor(db)
    val sourceIds = sourceAccessor.sourceIds
    val positions = EntryAccessor.createPositionMap(db, sourceIds)

    val accessor = EntryAccessor.forEntryOutline(db, sourceIds, positions)
    val entries = (0 to accessor.length - 1).flatMap(accessor.findAt)

    assertEquals(true, entries.exists {
      case UnreadEntryRow(EntryContent(entry)) =>
        entry.shortTitle == "5-1 entry title"
      case _ =>
        false
    })
    assertEquals(false, entries.exists {
      case UnreadEntryRow(EntryContent(entry)) =>
        entry.shortTitle == "3-1 entry title"
      case _ =>
        false
    })
  }
  @Test
  def testQueryPlanForEntryPosition() = {
    val context = RuntimeEnvironment.application
    DummyFactory.createDummies(context)(5)

    val db = new LinenOpenHelper(context).getReadableDatabase
    val Some(sourceAccessor) = inspectSourceAccessor(db)
    val sourceIds = sourceAccessor.sourceIds
    val query = EntryAccessor.createPositionQuery(sourceIds)
    val plans = QueryExplainer(db).explain(query)

//    println(query.sql)
//    plans foreach println

    assertEquals("USE TEMP B-TREE",
      false, plans.exists(_.detail contains "USE TEMP B-TREE"))
  }

  @Test
  def testUnreadRowKind() = {

    val context = RuntimeEnvironment.application
    DummyFactory.createDummies(context)(5)

    val db = new LinenOpenHelper(context).getReadableDatabase
    val Some(sourceAccessor) = inspectSourceAccessor(db)
    val sourceIds = sourceAccessor.sourceIds
    val positions = EntryAccessor.createPositionMap(db, sourceIds)
    val accessor = EntryAccessor.forEntryOutline(db, sourceIds, positions)

    assertEquals(Some(SourceKind), accessor.findKindAt(0))
    assertEquals(Some(EntryKind), accessor.findKindAt(1))
  }
  def inspectSourceAccessor(db: SQLiteDatabase) = {
    for {
      accountId <- AccountAccessor.findCurrentAccountId(db)
      channel <- MyChannelAccessor.createForDebug(db, accountId).findAt(0).collect {
        case x: MyChannel => x
      }
      either = AccessorLoader.inspectSourceAccessor(db, accountId, channel.channelId)
      accessor <- either.right.toOption
    } yield accessor
  }
}


@Config(manifest=Config.NONE)
@RunWith(classOf[RobolectricTestRunner])
class UnreadSourceAccessorTest extends JUnitSuiteLike {
  @Test
  def testQueryForNotRatedSources() = {
    val context = RuntimeEnvironment.application
    val helper = new LinenOpenHelper(context)
    val fixture = new UnreadSourceFixture(helper)

    val db = helper.getWritableDatabase

//    showProcess(db, fixture)

    val Success(accessor) = UnreadSourceAccessor.create(
      db,
      fixture.account2.accountId,
      fixture.channel1.channelId
    )
    val sources = (0 to accessor.length - 1).flatMap(accessor.findAt) collect {
      case UnreadSourceRow(x: UnreadSource) => x
    }
    // default rating is 100
    assertEquals(Seq(100, 100), sources.map(_.rating))
    assertEquals(Seq("description2", "description1"), sources.map(_.description))
    assertEquals(
      Seq(fixture.entryId2_1, fixture.entryId1_2),
      sources.map(_.latestEntryId)
    )
  }
  private def showProcess(db: SQLiteDatabase, fixture: UnreadSourceFixture): Unit = {
    dump(db, UnreadSourceAccessorQueries.sql2,
      Array(
        fixture.channel1.channelId,
        fixture.account2.accountId
      )
    )
    dump(db, UnreadSourceAccessorQueries.sql3,
      Array(
        fixture.channel1.channelId,
        fixture.account2.accountId
      )
    )
    dump(db, UnreadSourceAccessorQueries.sql4,
      Array(
        fixture.channel1.channelId,
        fixture.account2.accountId
      )
    )
    dump(db, UnreadSourceAccessorQueries.sql5,
      Array(
        fixture.channel1.channelId,
        fixture.account2.accountId,
        fixture.account2.accountId
      )
    )
  }

  private def dump[A](db: SQLiteDatabase, sql: String, args: Array[A]) = {
    println("-------")
    println(sql)
    val cursor0 = db.rawQuery(sql, args.map(_.toString))
    DebugTools.toMaps(cursor0).foreach(println)
  }
}
