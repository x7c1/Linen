package x7c1.linen.modern.accessor

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.{RuntimeEnvironment, RobolectricTestRunner}
import org.robolectric.annotation.Config
import org.scalatest.junit.JUnitSuiteLike
import x7c1.linen.modern.init.DummyFactory


@Config(manifest=Config.NONE)
@RunWith(classOf[RobolectricTestRunner])
class ChannelAccessorTest extends JUnitSuiteLike {

  @Test
  def testFindFirstId() = {
    val context = RuntimeEnvironment.application
    DummyFactory.createDummies(context)(5)

    val db = new LinenOpenHelper(context).getWritableDatabase
    val Some(accountId) = AccountAccessor.create(db).findFirstId()
    val Some(channelId) = ChannelAccessor.create(db, accountId).findFirstId()
    assertEquals(2, channelId)

    val channelId2 = ChannelAccessor.create(db, accountId = 123).findFirstId()
    assertEquals(None, channelId2)
  }

  @Test
  def testLength() = {
    val context = RuntimeEnvironment.application
    DummyFactory.createDummies(context)(5)

    val db = new LinenOpenHelper(context).getWritableDatabase
    val Some(accountId) = AccountAccessor.create(db).findFirstId()
    val length = ChannelAccessor.create(db, accountId).length
    assertEquals(2, length)
  }

  @Test
  def testFindAt() = {
    val context = RuntimeEnvironment.application
    DummyFactory.createDummies(context)(5)

    val db = new LinenOpenHelper(context).getWritableDatabase
    val Some(accountId) = AccountAccessor.create(db).findFirstId()
    val Some(c1) = ChannelAccessor.create(db, accountId).findAt(0)
    assertEquals("sample channel name2", c1.name)

    val Some(c2) = ChannelAccessor.create(db, accountId).findAt(1)
    assertEquals("sample channel name1", c2.name)

    val c3 = ChannelAccessor.create(db, accountId).findAt(2)
    assertEquals(None, c3)
  }
}
