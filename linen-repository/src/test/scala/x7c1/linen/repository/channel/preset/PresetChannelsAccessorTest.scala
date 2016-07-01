package x7c1.linen.repository.channel.preset

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.{RobolectricTestRunner, RuntimeEnvironment}
import org.scalatest.junit.JUnitSuiteLike
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.repository.account.setup.ClientAccountSetup
import x7c1.linen.repository.channel.subscribe.ChannelSubscriber
import x7c1.linen.repository.preset.PresetFactory

@Config(manifest=Config.NONE)
@RunWith(classOf[RobolectricTestRunner])
class PresetChannelsAccessorTest extends JUnitSuiteLike {

  @Test
  def testSetup1() = {
    val context = RuntimeEnvironment.application
    val helper = new DatabaseHelper(context)

    val factory = new PresetFactory(helper)
    factory.setupJapanesePresets()

    val Right(client) = ClientAccountSetup(helper).findOrCreate()
    val Right(accessor) = AllPresetChannelsAccessor.create(
      clientAccountId = client.accountId,
      helper = helper
    )
    assertEquals(3, accessor.length)
    assertEquals("Tech", accessor.findAt(0).get.name)
    assertEquals("Game", accessor.findAt(1).get.name)
    assertEquals(false, accessor.findAt(0).get.isSubscribed)
  }

  @Test
  def testSubscribeChannel() = {
    val context = RuntimeEnvironment.application
    val helper = new DatabaseHelper(context)

    val factory = new PresetFactory(helper)
    factory.setupJapanesePresets()

    val Right(client) = ClientAccountSetup(helper).findOrCreate()
    def channelAt(n: Int) = {
      val Right(accessor) = AllPresetChannelsAccessor.create(
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
