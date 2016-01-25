package x7c1.linen.modern.accessor

import android.database.Cursor
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.{RobolectricTestRunner, RuntimeEnvironment}
import org.scalatest.junit.JUnitSuiteLike
import x7c1.linen.modern.struct.{Channel, Date}

@Config(manifest=Config.NONE)
@RunWith(classOf[RobolectricTestRunner])
class SettingSourceAccessorTest extends JUnitSuiteLike {

  @Test
  def testQueryForSources() = {

    val context = RuntimeEnvironment.application
    val helper = new LinenOpenHelper(context)
    val db = helper.getWritableDatabase

    val factory = new SampleFactory(helper)
    val account = factory.setupAccount()
    val channel = factory.setupChannel(account)
    val channelOwner = new ChannelOwner(db, channel.channelId, account.accountId)

    channelOwner addSource ChannelSourceParts(
      url = "http://example.com/1",
      title = "hoge1",
      description = "piyo1",
      rating = 99
    )
    channelOwner addSource ChannelSourceParts(
      url = "http://example.com/2",
      title = "hoge2",
      description = "piyo2",
      rating = 88
    )

    val cursor = new SettingSourceAccessorFactory(db, account.accountId).
      createCursor(channel.channelId)

    val maps = toMaps(cursor)
    assertEquals(Seq("hoge2", "hoge1"), maps map {_("title")})
    assertEquals(Seq("88", "99"), maps map {_("rating")})
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
  lazy val writable = helper.writableDatabase
  lazy val readable = helper.readable

  def setupAccount(): Account = {
    val account = AccountAccessor.create(db) findAt 0 getOrElse {
      val Right(id) = writable insert AccountParts(
        nickname = s"sample-user",
        biography = s"sample-biography",
        createdAt = Date.current()
      )
      Account(accountId = id)
    }
    account
  }

  def setupChannel(account: Account): Channel = {
    val channelAccessor = ChannelAccessor.create(db, account.accountId)
    channelAccessor findAt 0 getOrElse {
      val Right(id) = writable insert ChannelParts(
        accountId = account.accountId,
        name = s"sample channel name",
        description = s"sample channel description",
        createdAt = Date.current()
      )
      val Right(Some(channel)) = readable.selectOne[Channel](id)
      channel
    }
  }
}
