package x7c1.linen.modern.accessor

import android.database.Cursor
import org.junit.Assert.assertEquals
import org.junit.runner.RunWith
import org.junit.{Assert, Test}
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

    val cursor3 = SourceBuffer createCursor context
    val rows = toMaps(cursor3)
    assertEquals(
      Seq("5", "4", "2", "1"),
      rows map {_("source_id")}
    )
    assertEquals(
      Seq("title-5", "title-4", "title-2", "title-1"),
      rows map {_("title")}
    )
    assertEquals(
      Seq("description-5", "description-4", "description-2", "description-1"),
      rows map {_("description")}
    )
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
}
