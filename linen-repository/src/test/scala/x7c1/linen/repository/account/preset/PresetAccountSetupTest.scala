package x7c1.linen.repository.account.preset

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.{RobolectricTestRunner, RuntimeEnvironment}
import org.scalatest.junit.JUnitSuiteLike
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.database.struct.{account_tag_map, account_tags}
import x7c1.linen.repository.account.setup.PresetAccountSetup
import x7c1.linen.repository.source.setting.SampleFactory

@Config(manifest=Config.NONE)
@RunWith(classOf[RobolectricTestRunner])
class PresetAccountSetupTest extends JUnitSuiteLike {

  @Test
  def testSetup1() = {
    val context = RuntimeEnvironment.application
    val helper = new DatabaseHelper(context)
    val factory = new SampleFactory(helper)
    val firstAccount = factory.createAccount()

    val Right(account1) = PresetAccountSetup(helper).findOrCreate()
    val Right(account2) = PresetAccountSetup(helper).findOrCreate()
    assertEquals(true, account1 == account2)

    val Right(Some(map)) = helper.selectorOf[account_tag_map].findBy(account1).toEither
    val Right(Some(tag)) = helper.readable.find[account_tags].by(map.account_tag_id).toEither
    assertEquals("preset", tag.tag_label)

    val Right(x) = helper.selectorOf[account_tag_map].findBy(firstAccount).toEither
    assertEquals(None, x)
  }

  @Test
  def testSetup2() = {
    val context = RuntimeEnvironment.application
    val helper = new DatabaseHelper(context)

    val Right(account1) = PresetAccountSetup(helper).findOrCreate()
    val Right(account2) = PresetAccountSetup(helper).findOrCreate()
    assertEquals(true, account1 == account2)

    val Right(Some(map)) = helper.selectorOf[account_tag_map].findBy(account1).toEither
    val Right(Some(tag)) = helper.readable.find[account_tags].by(map.account_tag_id).toEither
    assertEquals("preset", tag.tag_label)

    val factory = new SampleFactory(helper)
    val firstAccount = factory.createAccount()
    val Right(x) = helper.selectorOf[account_tag_map].findBy(firstAccount).toEither
    assertEquals(None, x)
  }

}
