package x7c1.linen.modern.accessor.setting

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.{RobolectricTestRunner, RuntimeEnvironment}
import org.scalatest.junit.JUnitSuiteLike
import x7c1.linen.modern.accessor.LinenOpenHelper
import x7c1.linen.modern.accessor.database.ChannelSubscriber
import x7c1.linen.modern.accessor.preset.ClientAccountSetup
import x7c1.linen.modern.init.updater.PresetFactory

@Config(manifest=Config.NONE)
@RunWith(classOf[RobolectricTestRunner])
class PresetChannelsAccessorTest extends JUnitSuiteLike {

  @Test
  def testSetup1() = {
    val context = RuntimeEnvironment.application
    val helper = new LinenOpenHelper(context)

    val factory = new PresetFactory(helper)
    factory.setupJapanesePresets()

    val Right(client) = ClientAccountSetup(helper).findOrCreate()
    val Right(accessor) = PresetChannelsAccessor.create(
      clientAccountId = client.accountId,
      helper = helper
    )
    assertEquals(2, accessor.length)
    assertEquals("Tech", accessor.findAt(0).get.name)
    assertEquals(false, accessor.findAt(0).get.isSubscribed)
  }

  @Test
  def testSubscribeChannel() = {
    val context = RuntimeEnvironment.application
    val helper = new LinenOpenHelper(context)

    val factory = new PresetFactory(helper)
    factory.setupJapanesePresets()

    val Right(client) = ClientAccountSetup(helper).findOrCreate()
    def channelAt(n: Int) = {
      val Right(accessor) = PresetChannelsAccessor.create(
        clientAccountId = client.accountId,
        helper = helper
      )
      val Some(channel) = accessor findAt n
      channel
    }
    assertEquals(false, channelAt(0).isSubscribed)
    assertEquals(false, channelAt(1).isSubscribed)

    val subscriber = new ChannelSubscriber(account = client, helper)
    subscriber subscribe channelAt(0).channelId

    assertEquals(true, channelAt(0).isSubscribed)
    assertEquals(false, channelAt(1).isSubscribed)
  }

}
