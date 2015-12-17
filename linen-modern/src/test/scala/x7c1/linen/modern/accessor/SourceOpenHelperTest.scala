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
  def testSample() = {
    val context = RuntimeEnvironment.application
    DummyFactory.createDummies(context)(5)

    val helper = new LinenOpenHelper(context)
    val db = helper.getWritableDatabase

    val cursor = db.rawQuery("SELECT * FROM sources LIMIT ?", Array("2"))
    Assert.assertEquals(2, cursor.getCount)
  }

  @Test
  def testQueryForSourceArea() = {
    val context = RuntimeEnvironment.application
    DummyFactory.createDummies(context)(5)

    val helper = new LinenOpenHelper(context)
    val db = helper.getWritableDatabase
    val listId = SourceAccessor.findFirstListId(db)
    val accountId = SourceAccessor.findFirstAccountId(db)
    val cursor3 = SourceAccessor.createCursor(db, listId, accountId)
    val rows = toMaps(cursor3)

    rows.map(prettyPrint) foreach println

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

    val helper = new LinenOpenHelper(context)
    val db = helper.getWritableDatabase

    val sql4 = EntryAccessor.createSql4("entries.content")
    val cursor = EntryAccessor.createCursor(db, sql4)
    val rows = toMaps(cursor)

    /*
    rows.map(prettyPrint).foreach(println)
    */

    val found1 = rows exists { _("title") == "5-1 entry title" }
    assertEquals(true, found1)

    val found2 = rows exists { _("title") == "3-1 entry title" }
    assertEquals(false, found2)
  }
  @Test
  def testQueryToCountEntry() = {
    val context = RuntimeEnvironment.application
    DummyFactory.createDummies(context)(5)

    val helper = new LinenOpenHelper(context)
    val db = helper.getWritableDatabase

    val sql4 = EntryAccessor.createSql4("entries.content")
    val cursor = EntryAccessor.createCounterCursor(db, sql4)
    val rows = toMaps(cursor)
    val actual = rows.map(_("count")).map(_.toInt)
    assertEquals(Seq(10,10,10,10), actual)
  }

  @Test
  def testCalculateSourcePosition(): Unit = {
    val context = RuntimeEnvironment.application
    DummyFactory.createDummies(context)(5)

    val helper = new LinenOpenHelper(context)
    val db = helper.getWritableDatabase

    val sql4 = EntryAccessor.createSql4("entries.content")
    val positions = EntryAccessor.createSourcePositionMap(db, sql4)
    assertEquals(0, positions(5))
    assertEquals(10, positions(4))
    assertEquals(20, positions(2))
    assertEquals(30, positions(1))

    intercept[NoSuchElementException](positions(3))
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
