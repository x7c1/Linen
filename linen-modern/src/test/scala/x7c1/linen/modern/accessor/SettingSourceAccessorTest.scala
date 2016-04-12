package x7c1.linen.modern.accessor

import android.database.Cursor
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.{RobolectricTestRunner, RuntimeEnvironment}
import org.scalatest.junit.JUnitSuiteLike
import x7c1.linen.database.{ChannelParts, QueryExplainer, LinenOpenHelper}
import x7c1.linen.domain.Date
import x7c1.linen.modern.struct.Channel

@Config(manifest=Config.NONE)
@RunWith(classOf[RobolectricTestRunner])
class SettingSourceAccessorTest extends JUnitSuiteLike {

  @Test
  def testQueryForSources() = {

    val context = RuntimeEnvironment.application
    val helper = new LinenOpenHelper(context)
    val db = helper.getWritableDatabase

    val factory = new SampleFactory(helper)
    val account1 = factory.createAccount()
    val account2 = factory.createAccount()
    val channel1 = factory.createChannel(account1)
    val channel2 = factory.createChannel(account2)

    val channelOwner1 = new ChannelOwner(db, channel1.channelId, account1.accountId)
    val Right(sourceId1) = channelOwner1 addSource ChannelSourceParts(
      url = "http://example.com/1",
      title = "hoge1",
      description = "piyo1",
      rating = 99
    )
    channelOwner1 addSource ChannelSourceParts(
      url = "http://example.com/2",
      title = "hoge2",
      description = "piyo2",
      rating = 88
    )
    val subscriber1 = new SourceSubscriber(db, account2.accountId, sourceId1)
    subscriber1 updateRating 55

    val accessorFactory = new SettingSourceAccessorFactory(db, account1.accountId)
    val cursor = accessorFactory.
      createCursor(channel1.channelId)

    val maps = toMaps(cursor)
//    maps foreach println
    assertEquals(Seq("hoge2", "hoge1"), maps map {_("title")})
    assertEquals(Seq("88", "99"), maps map {_("rating")})

    val plans = QueryExplainer(db).
      explain(accessorFactory createQuery channel1.channelId)

    assertEquals("USE TEMP B-TREE",
      false, plans.exists(_.detail contains "USE TEMP B-TREE"))
  }

  def toMaps(cursor: Cursor): Seq[Map[String, String]] = {
    (0 to cursor.getCount - 1) map { i =>
      cursor moveToPosition i
      val pairs = (0 to cursor.getColumnCount - 1) map { i =>
        val column = cursor.getColumnName(i)
        val value = cursor.getString(i)
        column -> value
      }
      pairs.toMap
    }
  }
}

class SampleFactory (helper: LinenOpenHelper){

  lazy val db = helper.getWritableDatabase
  lazy val writable = helper.writable
  lazy val readable = helper.readable

  def createAccount(): Account = {
    val Right(id) = writable insert AccountParts(
      nickname = s"sample-user",
      biography = s"sample-biography",
      createdAt = Date.current()
    )
    Account(accountId = id)

  }

  def createChannel(owner: Account): Channel = {
    val Right(id) = writable insert ChannelParts(
      accountId = owner.accountId,
      name = s"sample channel name",
      description = s"sample channel description",
      createdAt = Date.current()
    )
    val Right(Some(channel)) = readable.find[Channel].by(id)
    channel
  }
}
