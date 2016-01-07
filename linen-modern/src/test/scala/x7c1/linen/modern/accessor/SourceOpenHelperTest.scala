package x7c1.linen.modern.accessor

import android.database.Cursor
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.{RobolectricTestRunner, RuntimeEnvironment}
import org.scalatest.junit.JUnitSuiteLike
import x7c1.linen.modern.init.DummyFactory

@Config(manifest=Config.NONE)
@RunWith(classOf[RobolectricTestRunner])
class SourceOpenHelperTest extends JUnitSuiteLike {

  @Test
  def testQueryForSourceArea() = {
    val context = RuntimeEnvironment.application
    DummyFactory.createDummies(context)(5)

    val helper = new LinenOpenHelper(context)
    val db = helper.getWritableDatabase
    val Some(accountId) = AccountAccessor.create(db) findAt 0 map (_.accountId)
    val Some(channelId) = ChannelAccessor.create(db, accountId).findFirstId()
    val cursor3 = SourceAccessor.createCursor(db, channelId, accountId)
    val rows = toMaps(cursor3)

    //rows.map(prettyPrint) foreach println

    assertEquals(
      Seq("5", "4", "2", "1"),
      rows map {_("source_id")}
    )
    assertEquals(
      Seq("5-title", "4-title", "2-title", "1-title"),
      rows map {_("title")}
    )
  }
  @Test
  def testQueryForEntryArea() = {

    val context = RuntimeEnvironment.application
    DummyFactory.createDummies(context)(5)

    /*
    val accessor0 = SourceAccessor.create(context)
    val sources = (0 to accessor0.length - 1).map(accessor0.findAt)
    val sourceIds = sources.flatMap(_.map(_.id))
    val cursor = EntryAccessor.createOutlineCursor(context, sourceIds)
    val rows = toMaps(cursor)
    rows.map(prettyPrint) foreach println
    */

    val db = new LinenOpenHelper(context).getReadableDatabase
    val Right(sourceAccessor) = SourceAccessor create db
    val sourceIds = (0 to sourceAccessor.length - 1).map(sourceAccessor.findAt).flatMap(_.map(_.id))
    val positions = EntryAccessor.createPositionMap(db, sourceIds)

    val accessor = EntryAccessor.forEntryOutline(db, sourceIds, positions)
    val entries = (0 to accessor.length - 1).flatMap(accessor.findAt)

    assertEquals(true, entries.exists(_.shortTitle == "5-1 entry title"))
    assertEquals(false, entries.exists(_.shortTitle == "3-1 entry title"))

    assertEquals(Seq(5,4,2,1), entries.map(_.sourceId).distinct)
  }
  @Test
  def testQueryToCountEntry() = {
    val context = RuntimeEnvironment.application
    val db = new LinenOpenHelper(context).getReadableDatabase
    DummyFactory.createDummies(context)(5)

    val Right(accessor0) = SourceAccessor create db
    val sources = (0 to accessor0.length - 1).map(accessor0.findAt)
    val sourceIds = sources.flatMap(_.map(_.id))
    val cursor = EntryAccessor.createPositionCursor(db, sourceIds)
    val rows = toMaps(cursor)
//    rows.map(prettyPrint) foreach println
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
