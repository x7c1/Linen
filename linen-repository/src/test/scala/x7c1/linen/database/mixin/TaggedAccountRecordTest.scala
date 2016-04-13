package x7c1.linen.database.mixin

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.{RobolectricTestRunner, RuntimeEnvironment}
import org.scalatest.junit.JUnitSuiteLike
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.database.struct.PresetLabel
import x7c1.wheat.modern.database.QueryExplainer


@Config(manifest=Config.NONE)
@RunWith(classOf[RobolectricTestRunner])
class TaggedAccountRecordTest extends JUnitSuiteLike {
  @Test
  def testPlan() = {
    val context = RuntimeEnvironment.application
    val helper = new DatabaseHelper(context)
    val db = helper.getReadableDatabase

    val query = TaggedAccountRecord select PresetLabel
    val plans = QueryExplainer(db).explain(query)

    //    plans foreach println

    assertEquals("USE TEMP B-TREE",
      false, plans.exists(_.useTempBtree))
  }
}
