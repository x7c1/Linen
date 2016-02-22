package x7c1.linen.modern.accessor

import android.database.Cursor
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.{RobolectricTestRunner, RuntimeEnvironment}
import org.scalatest.junit.JUnitSuiteLike
import x7c1.linen.modern.init.dev.DummyFactory
import x7c1.linen.modern.init.unread.AccessorLoader

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
    val sources = (0 to accessor.length - 1).flatMap(accessor.findAt)
    assertEquals(Seq(33, 11), sources.map(_.rating))
    assertEquals(Seq("description2", "description1"), sources.map(_.description))
    assertEquals(
      Seq(fixture.entryId2_1, fixture.entryId1_2),
      sources.map(_.latestEntryId)
    )
  }
  @Test
  def testExplain() = {
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
    val Right(sourceAccessor) = AccessorLoader.inspectSourceAccessor(db).toEither
    val sourceIds = sourceAccessor.sourceIds
    val positions = EntryAccessor.createPositionMap(db, sourceIds)

    val accessor = EntryAccessor.forEntryOutline(db, sourceIds, positions)
    val entries = (0 to accessor.length - 1).flatMap(accessor.findAt)

    assertEquals(true, entries.exists {
      case EntryRow(Right(entry)) =>
        entry.shortTitle == "5-1 entry title"
      case _ =>
        false
    })
    assertEquals(false, entries.exists {
      case EntryRow(Right(entry)) =>
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
    val Right(sourceAccessor) = AccessorLoader.inspectSourceAccessor(db).toEither
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
    val Right(sourceAccessor) = AccessorLoader.inspectSourceAccessor(db).toEither
    val sourceIds = sourceAccessor.sourceIds
    val positions = EntryAccessor.createPositionMap(db, sourceIds)
    val accessor = EntryAccessor.forEntryOutline(db, sourceIds, positions)

    assertEquals(Some(SourceKind), accessor.findKindAt(0))
    assertEquals(Some(EntryKind), accessor.findKindAt(1))
  }

  def showTable(tableName: String) = {
    println(s"====== tableName : $tableName")
    val context = RuntimeEnvironment.application
    val helper = new LinenOpenHelper(context)
    val db = helper.getReadableDatabase
    val cursor = db.rawQuery(s"SELECT * FROM $tableName LIMIT 100", Array())
    dumpCursor(cursor)
  }
  def dumpCursor(cursor: Cursor) = {
    println("=====")
    while(cursor.moveToNext()){
      println("-----")
      (0 to cursor.getColumnCount - 1) foreach { i =>
        val column = cursor.getColumnName(i)
        val value = cursor.getString(i)
        println(s"$column : $value")
      }
    }
  }
  def toMaps(cursor: Cursor): Seq[Map[String, String]] = {
    (0 to cursor.getCount - 1) map { i =>
      cursor moveToPosition i
      val pairs = (0 to cursor.getColumnCount - 1) map { i =>
        val column = cursor.getColumnName(i)
        val value = cursor.getString(i)
        column -> value
      }
      pairs.toMap
    }
  }
  def prettyPrint(target: Map[String, String]) = {
    target.
      map{ case (k, v) => s"$k -> $v" }.
      map("  " + _).
      mkString("Map(\n", ",\n", "\n)")
  }
}
