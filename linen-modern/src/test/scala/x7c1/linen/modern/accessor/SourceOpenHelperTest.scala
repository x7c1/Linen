package x7c1.linen.modern.accessor

import org.junit.{Assert, Test}
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.{RobolectricTestRunner, RuntimeEnvironment}
import org.scalatest.junit.JUnitSuiteLike
import x7c1.linen.modern.init.DummyFactory

@Config(manifest=Config.NONE)
@RunWith(classOf[RobolectricTestRunner])
class SourceOpenHelperTest extends JUnitSuiteLike {

  @Test
  def testHoge() = {
    val context = RuntimeEnvironment.application
    DummyFactory.createDummies(context)

    val helper = new LinenOpenHelper(context)
    val db = helper.getWritableDatabase

//    val cursor = db.query("sources", null, null, null, null, null, null, null)
    val cursor = db.rawQuery("SELECT * FROM sources LIMIT ?", Array("2"))
    Assert.assertEquals(2, cursor.getCount)
  }

  @Test
  def testFuga() = {
    /*
    val context = RuntimeEnvironment.application
    DummyFactory.createDummies(context)

    val helper = new LinenOpenHelper(context)
    val db = helper.getWritableDatabase

    val cursor = db.query("sources", null, null, null, null, null, null, null)
    Assert.assertEquals(5, cursor.getCount)
    */
  }
}
