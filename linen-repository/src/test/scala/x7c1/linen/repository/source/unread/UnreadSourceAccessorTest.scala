package x7c1.linen.repository.source.unread

import android.database.sqlite.SQLiteDatabase
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.{RobolectricTestRunner, RuntimeEnvironment}
import org.scalatest.junit.JUnitSuiteLike
import x7c1.linen.database.DebugTools
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.database.struct.EntryRecord

import scala.util.Success


@Config(manifest=Config.NONE)
@RunWith(classOf[RobolectricTestRunner])
class UnreadSourceAccessorTest extends JUnitSuiteLike {
  @Test
  def testQueryForNotRatedSources() = {
    val context = RuntimeEnvironment.application
    val helper = new DatabaseHelper(context)
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

    import x7c1.linen.database.struct.EntryIdentifier._
    val latestEntries = sources.map(_.latestEntryId).flatMap { entryId =>
      helper.readable.selectorOf[EntryRecord].find(entryId).toOption
    }
    assertEquals(
      Seq("sample-entry2-1", "sample-entry1-2"),
      latestEntries.map(_.title)
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
