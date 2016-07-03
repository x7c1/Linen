package x7c1.linen.repository.notification

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.{RobolectricTestRunner, RuntimeEnvironment}
import org.scalatest.junit.JUnitSuiteLike
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.database.struct.NotificationContentKind.ChannelLoaderKind
import x7c1.linen.database.struct.NotificationIdRecord
import x7c1.linen.database.struct.NotificationKey.ChannelLoaderKey
import x7c1.linen.repository.source.setting.SampleFactory
import x7c1.linen.testing.{AllowTraversingAll, LogSetting}
import x7c1.wheat.modern.either.OptionRight


@Config(manifest=Config.NONE)
@RunWith(classOf[RobolectricTestRunner])
class NotificationIdStoreTest extends JUnitSuiteLike with LogSetting with AllowTraversingAll {

  @Test
  def testSetupFor() = {
    val context = RuntimeEnvironment.application
    val helper = new DatabaseHelper(context)
    val factory = new SampleFactory(helper)
    val account1 = factory.createAccount()
    val account2 = factory.createAccount()
    val channel1 = factory createChannel account1

    val target = ChannelLoaderKey(account1 -> channel1)
    def findTarget() = helper.selectorOf[NotificationIdRecord] findBy target

    val OptionRight(before) = findTarget()
    assertEquals(None, before)

    val either = NotificationIdStore(helper) getOrCreate target
    assertEquals(true, either.isRight)

    val OptionRight(Some(after)) = findTarget()
    assertEquals(either.right.get, after.notification_id)
    assertEquals(ChannelLoaderKind, after.notification_content_kind.typed)

    val Right(idAgain) = NotificationIdStore(helper) getOrCreate target
    assertEquals(after.notification_id, idAgain)

    val OptionRight(other) =
      helper.selectorOf[NotificationIdRecord] findBy ChannelLoaderKey(
        account2 -> channel1
      )
    assertEquals(None, other)
  }
}
