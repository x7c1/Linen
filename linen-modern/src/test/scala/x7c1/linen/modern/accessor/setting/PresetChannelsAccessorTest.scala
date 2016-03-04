package x7c1.linen.modern.accessor.setting

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.{RuntimeEnvironment, RobolectricTestRunner}
import org.robolectric.annotation.Config
import org.scalatest.junit.JUnitSuiteLike
import x7c1.linen.modern.accessor.LinenOpenHelper
import x7c1.linen.modern.accessor.preset.ClientAccountSetup
import x7c1.linen.modern.init.updater.PresetFactory

@Config(manifest=Config.NONE)
@RunWith(classOf[RobolectricTestRunner])
class PresetChannelsAccessorTest extends JUnitSuiteLike {

  @Test
  def testSetup1() = {
    val context = RuntimeEnvironment.application
    val helper = new LinenOpenHelper(context)

    val factory = new PresetFactory(context, helper)
    factory.setupJapanesePresets()

    val Right(client) = ClientAccountSetup(helper).findOrCreate()
    val Right(accessor) = PresetChannelsAccessor.create(
      clientAccountId = client.accountId,
      helper = helper
    )
    assertEquals(2, accessor.length)
    assertEquals("Game", accessor.findAt(0).get.name)
    assertEquals(false, accessor.findAt(0).get.isSubscribed)
  }
}
