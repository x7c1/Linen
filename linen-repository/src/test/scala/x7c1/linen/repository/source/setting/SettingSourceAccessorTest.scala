package x7c1.linen.repository.source.setting

import android.database.Cursor
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.{RobolectricTestRunner, RuntimeEnvironment}
import org.scalatest.junit.JUnitSuiteLike
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.database.struct.{AccountParts, ChannelParts, ChannelStatusKey}
import x7c1.linen.repository.account.dev.DevAccount
import x7c1.linen.repository.date.Date
import x7c1.wheat.modern.database.QueryExplainer

@Config(manifest=Config.NONE)
@RunWith(classOf[RobolectricTestRunner])
class SettingSourceAccessorTest extends JUnitSuiteLike {

  @Test
  def testQueryForSources() = {

    val context = RuntimeEnvironment.application
    val helper = new DatabaseHelper(context)
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

    val query = SettingSource.traverse queryAbout ChannelStatusKey(
      channelId = channel1.channelId,
      accountId = account1.accountId
    )
    val cursor = db.rawQuery(query.sql, query.selectionArgs)
    val maps = toMaps(cursor)
//    maps foreach println
    assertEquals(Seq("hoge2", "hoge1"), maps map {_("title")})
    assertEquals(Seq("88", "99"), maps map {_("rating")})

    val plans = QueryExplainer(db).explain(query)
    assertEquals("USE TEMP B-TREE",
      false, plans.exists(_.useTempBtree))
  }

  def toMaps(cursor: Cursor): Seq[Map[String, String]] = {
    0 until cursor.getCount map { i =>
      cursor moveToPosition i
      val pairs = 0 until cursor.getColumnCount map { i =>
        val column = cursor.getColumnName(i)
        val value = cursor.getString(i)
        column -> value
      }
      pairs.toMap
    }
  }
}

class SampleFactory (helper: DatabaseHelper){

  lazy val db = helper.getWritableDatabase

  lazy val writable = helper.writable

  def createAccount(): DevAccount = {
    val Right(id) = writable insert AccountParts(
      nickname = s"sample-user",
      biography = s"sample-biography",
      createdAt = Date.current()
    )
    DevAccount(accountId = id)

  }

  def createChannel(owner: DevAccount): Channel = {
    val Right(id) = writable insert ChannelParts(
      accountId = owner.accountId,
      name = s"sample channel name",
      description = s"sample channel description",
      createdAt = Date.current()
    )
    val Right(Some(channel)) = helper.selectorOf[Channel].findBy(id).toEither
    channel
  }
}
