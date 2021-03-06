package x7c1.linen.repository.channel.my

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.{RobolectricTestRunner, RuntimeEnvironment}
import org.scalatest.junit.JUnitSuiteLike
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.repository.account.dev.AccountAccessor
import x7c1.linen.repository.dummy.DummyFactory


@Config(manifest=Config.NONE)
@RunWith(classOf[RobolectricTestRunner])
class MyChannelAccessorTest extends JUnitSuiteLike {

  @Test
  def testFindFirstId() = {
    val context = RuntimeEnvironment.application
    DummyFactory.createDummies(context)(5)

    val helper = new DatabaseHelper(context)
    val db = helper.getWritableDatabase
    val Some(accountId) = AccountAccessor.create(db) findAt 0 map (_.accountId)
    val Some(channelId) = MyChannelAccessor.createForDebug(helper, accountId) findAt 0 collect {
      case x: MyChannel => x.channelId
    }
    assertEquals(2, channelId)

    val channelId2 = MyChannelAccessor.createForDebug(helper, accountId = 123) findAt 0
    assertEquals(None, channelId2)
  }

  @Test
  def testLength() = {
    val context = RuntimeEnvironment.application
    DummyFactory.createDummies(context)(5)

    val helper = new DatabaseHelper(context)
    val db = helper.getWritableDatabase
    val Some(accountId) = AccountAccessor.create(db) findAt 0 map (_.accountId)
    val length = MyChannelAccessor.createForDebug(helper, accountId).length
    assertEquals(2, length)
  }

  @Test
  def testFindAt() = {
    val context = RuntimeEnvironment.application
    DummyFactory.createDummies(context)(5)

    val helper = new DatabaseHelper(context)
    val db = helper.getWritableDatabase
    val Some(accountId) = AccountAccessor.create(db) findAt 0 map (_.accountId)
    val Some(c1) = MyChannelAccessor.createForDebug(helper, accountId).findAt(0) collect {
      case x: MyChannel => x
    }
    assertEquals("sample channel name2", c1.name)

    val Some(c2) = MyChannelAccessor.createForDebug(helper, accountId).findAt(1) collect {
      case x: MyChannel => x
    }
    assertEquals("sample channel name1", c2.name)

    val c3 = MyChannelAccessor.createForDebug(helper, accountId).findAt(2)
    assertEquals(None, c3)
  }
}
