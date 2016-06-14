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
import x7c1.linen.repository.channel.order.ChannelOrderUpdater
import x7c1.linen.repository.source.setting.SampleFactory
import x7c1.linen.testing.{AllowTraversingAll, LogSetting}
import x7c1.wheat.modern.database.QueryExplainer
import x7c1.wheat.modern.observer.recycler.order.PositionedItems

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
  def testOrderUpdater() = {
    val context = RuntimeEnvironment.application
    val helper = new DatabaseHelper(context)
    val factory = new SampleFactory(helper)

    val account1 = factory.createAccount()
    val account2 = factory.createAccount()

    val Right(ch1) = ChannelCreator(helper, account1) createChannel InputToCreate(
      channelName = "foo1"
    )
    val Right(ch2) = ChannelCreator(helper, account1) createChannel InputToCreate(
      channelName = "foo2"
    )
    val Right(ch3) = ChannelCreator(helper, account1) createChannel InputToCreate(
      channelName = "foo3"
    )
    ChannelCreator(helper, account2) createChannel InputToCreate(
      channelName = "bar3"
    )
    locally {
      val Right(xs) = helper.selectorOf[SubscribedChannel] traverseOn account1
      assertEquals(Seq(0,0,0), xs.toSeq map (_.channelRank))
      assertEquals(Seq("foo1","foo2","foo3"), xs.toSeq map (_.name))
    }
    val updater = ChannelOrderUpdater[SubscribedChannel](helper.getWritableDatabase)
    updater.updateDefaultRanks(account1).left foreach { this fail _ }

    // move to middle
    locally {
      val Right(before) = helper.selectorOf[SubscribedChannel] traverseOn account1
      assertEquals(Seq(-2,-1,0), before.toSeq.map(_.channelRank).toIndexedSeq)
      assertEquals(Seq("foo3","foo2","foo1"), before.toSeq map (_.name))

      updater update PositionedItems(
        previous = before.findAt(2),
        current = before.findAt(0).get,
        next = before.findAt(1)
      )
      val Right(after) = helper.selectorOf[SubscribedChannel] traverseOn account1
      assertEquals(Seq("foo2","foo3","foo1"), after.toSeq map (_.name))
    }
    // move to tail
    locally {
      val Right(before) = helper.selectorOf[SubscribedChannel] traverseOn account1
      updater update PositionedItems(
        previous = before.findAt(2),
        current = before.findAt(0).get,
        next = None
      )
      val Right(after) = helper.selectorOf[SubscribedChannel] traverseOn account1
      assertEquals(Seq("foo3","foo1","foo2"), after.toSeq.map(_.name).toIndexedSeq)
    }
    // move to head
    locally {
      val Right(before) = helper.selectorOf[SubscribedChannel] traverseOn account1
      updater update PositionedItems(
        previous = None,
        current = before.findAt(2).get,
        next = before.findAt(0)
      )
      val Right(after) = helper.selectorOf[SubscribedChannel] traverseOn account1
      assertEquals(Seq("foo2","foo3","foo1"), after.toSeq.map(_.name).toIndexedSeq)
    }
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
