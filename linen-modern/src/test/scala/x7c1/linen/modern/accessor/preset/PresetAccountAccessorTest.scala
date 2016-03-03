package x7c1.linen.modern.accessor.preset

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.{RobolectricTestRunner, RuntimeEnvironment}
import org.scalatest.junit.JUnitSuiteLike
import x7c1.linen.modern.accessor.database.{account_tag_map, account_tags}
import x7c1.linen.modern.accessor.{SampleFactory, LinenOpenHelper}

@Config(manifest=Config.NONE)
@RunWith(classOf[RobolectricTestRunner])
class PresetAccountAccessorTest extends JUnitSuiteLike {

  @Test
  def testSetup1() = {
    val context = RuntimeEnvironment.application
    val helper = new LinenOpenHelper(context)
    val factory = new SampleFactory(helper)
    val firstAccount = factory.createAccount()

    val Right(account1) = PresetAccountAccessor(helper).setupAccount()
    val Right(account2) = PresetAccountAccessor(helper).setupAccount()
    assertEquals(true, account1 == account2)

    val Right(Some(map)) = helper.readable.find[account_tag_map] by account1.accountId
    val Right(Some(tag)) = helper.readable.find[account_tags].by(map.account_tag_id)
    assertEquals("preset", tag.tag_label)

    val Right(x) = helper.readable.find[account_tag_map].by(firstAccount.accountId)
    assertEquals(None, x)
  }

  @Test
  def testSetup2() = {
    val context = RuntimeEnvironment.application
    val helper = new LinenOpenHelper(context)

    val Right(account1) = PresetAccountAccessor(helper).setupAccount()
    val Right(account2) = PresetAccountAccessor(helper).setupAccount()
    assertEquals(true, account1 == account2)

    val Right(Some(map)) = helper.readable.find[account_tag_map] by account1.accountId
    val Right(Some(tag)) = helper.readable.find[account_tags].by(map.account_tag_id)
    assertEquals("preset", tag.tag_label)

    val factory = new SampleFactory(helper)
    val firstAccount = factory.createAccount()
    val Right(x) = helper.readable.find[account_tag_map].by(firstAccount.accountId)
    assertEquals(None, x)
  }

}
