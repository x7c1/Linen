package x7c1.linen.repository.channel.preset

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.{RobolectricTestRunner, RuntimeEnvironment}
import org.scalatest.junit.JUnitSuiteLike
import x7c1.linen.database.LinenOpenHelper
import x7c1.linen.database.struct.ChannelSubscriber
import x7c1.linen.repository.account.setup.ClientAccountSetup
import x7c1.linen.repository.preset.PresetFactory

@Config(manifest=Config.NONE)
@RunWith(classOf[RobolectricTestRunner])
class SelectedChannelsAccessorTest extends JUnitSuiteLike {
  @Test
  def testSetup1() = {
    val context = RuntimeEnvironment.application
    val helper = new LinenOpenHelper(context)

    val factory = new PresetFactory(helper)
    factory.setupJapanesePresets()

    val Right(client) = ClientAccountSetup(helper).findOrCreate()
    val Right(accessor) = SelectedPresetChannelsAccessor.create(
      clientAccountId = client.accountId,
      helper = helper
    )
    /* not selected yet */
    assertEquals(0, accessor.length)

    def channelAt(n: Int) = {
      val Right(accessor) = AllPresetChannelsAccessor.create(
        clientAccountId = client.accountId,
        helper = helper
      )
      val Some(channel) = accessor findAt n
      channel
    }
    val subscriber = new ChannelSubscriber(account = client, helper)
    subscriber subscribe channelAt(0).channelId
    subscriber subscribe channelAt(1).channelId

    val Right(updated) = SelectedPresetChannelsAccessor.create(
      clientAccountId = client.accountId,
      helper = helper
    )
    /* subscribed both of them */
    assertEquals(2, updated.length)
    assertEquals(Some(true), updated.findAt(0).map(_.isSubscribed))
    assertEquals(Some(true), updated.findAt(1).map(_.isSubscribed))

    subscriber unsubscribe channelAt(1).channelId
    val Right(updated2) = SelectedPresetChannelsAccessor.create(
      clientAccountId = client.accountId,
      helper = helper
    )
    /* unsubscribed one of them */
    assertEquals(Some(true), updated2.findAt(0).map(_.isSubscribed))
    assertEquals(Some(false), updated2.findAt(1).map(_.isSubscribed))
  }
}
