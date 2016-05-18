package x7c1.linen.repository.channel.subscribe

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.{RobolectricTestRunner, RuntimeEnvironment}
import org.scalatest.junit.JUnitSuiteLike
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.database.mixin.SubscribedChannelRecord
import x7c1.linen.repository.channel.my.ChannelCreator
import x7c1.linen.repository.channel.my.ChannelCreator.InputToCreate
import x7c1.linen.repository.source.setting.SampleFactory
import x7c1.linen.testing.{LogSetting, AllowTraversingAll}
import x7c1.wheat.modern.database.QueryExplainer

@Config(manifest=Config.NONE)
@RunWith(classOf[RobolectricTestRunner])
class SubscribedChannelTest extends JUnitSuiteLike with LogSetting with AllowTraversingAll {

  @Test
  def testTraverse() = {
    val context = RuntimeEnvironment.application
    val helper = new DatabaseHelper(context)
    val factory = new SampleFactory(helper)

    val account1 = factory.createAccount()
    val account2 = factory.createAccount()

    ChannelCreator(helper, account1) createChannel InputToCreate(
      channelName = "foo1"
    )
    ChannelCreator(helper, account1) createChannel InputToCreate(
      channelName = "foo2"
    )
    ChannelCreator(helper, account2) createChannel InputToCreate(
      channelName = "foo3"
    )
    val Right(sequence1) = helper.selectorOf[SubscribedChannel] traverseOn account1
    assertEquals(2, sequence1.length)
    assertEquals(true, sequence1.exists(_.name == "foo1"))
    assertEquals(true, sequence1.exists(_.name == "foo2"))

    val Right(sequence2) = helper.selectorOf[SubscribedChannel] traverseOn account2
    assertEquals(1, sequence2.length)
    assertEquals(true, sequence2.exists(_.name == "foo3"))
  }

  @Test
  def testQueryToTraverse() = {
    val context = RuntimeEnvironment.application
    val helper = new DatabaseHelper(context)
    val factory = new SampleFactory(helper)

    val account1 = factory.createAccount()
    val query = SubscribedChannelRecord.traversable query account1
    val plans = QueryExplainer(helper.getReadableDatabase) explain query

    assertEquals(
      "USE TEMP B-TREE",
      false,
      plans.exists(_.useTempBtree)
    )
  }
}
