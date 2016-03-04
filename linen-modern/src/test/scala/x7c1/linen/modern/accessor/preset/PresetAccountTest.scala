package x7c1.linen.modern.accessor.preset

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.{RobolectricTestRunner, RuntimeEnvironment}
import org.scalatest.junit.JUnitSuiteLike
import x7c1.linen.modern.accessor.database.PresetLabel
import x7c1.linen.modern.accessor.{LinenOpenHelper, QueryExplainer}

@Config(manifest=Config.NONE)
@RunWith(classOf[RobolectricTestRunner])
class PresetAccountTest extends JUnitSuiteLike {
  @Test
  def testPlan() = {
    val context = RuntimeEnvironment.application
    val helper = new LinenOpenHelper(context)
    val db = helper.getReadableDatabase

    val query = TaggedAccountRecord select PresetLabel
    val plans = QueryExplainer(db).explain(query)

//    plans foreach println

    assertEquals("USE TEMP B-TREE",
      false, plans.exists(_.useTempBtree))
  }
}
