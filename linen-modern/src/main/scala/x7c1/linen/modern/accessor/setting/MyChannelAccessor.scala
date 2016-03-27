package x7c1.linen.modern.accessor.setting

import android.database.sqlite.SQLiteDatabase
import x7c1.linen.modern.accessor.database.{ChannelRecord, ChannelStatusRecord}
import x7c1.linen.modern.struct.Date
import x7c1.wheat.macros.database.{TypedCursor, TypedFields}

trait MyChannelAccessor {
  def accountId: Long
  def findAt(position: Int): Option[SettingMyChannel]
  def length: Int
}

object MyChannelAccessor {
  def create(db: SQLiteDatabase, accountId: Long): MyChannelAccessor = {
    new MyChannelAccessorImpl(db, accountId)
  }
  private class MyChannelAccessorImpl(
    db: SQLiteDatabase,
    override val accountId: Long) extends MyChannelAccessor {

    private lazy val rawCursor = {
      val sql1 =
        """SELECT
          | _id,
          | name,
          | description,
          | IFNULL(c2.subscribed, 0) AS subscribed,
          | c1.created_at AS created_at
          |FROM channels AS c1
          | LEFT JOIN channel_statuses AS c2
          |   ON c1._id = c2.channel_id AND c2.account_id = ?
          |WHERE c1.account_id = ?
          |ORDER BY c1._id DESC""".stripMargin

      db.rawQuery(sql1, Array(accountId.toString, accountId.toString))
    }
    private lazy val cursor = TypedCursor[MyChannelRecord](rawCursor)

    override def findAt(position: Int) =
      cursor.moveToFind(position){
        SettingMyChannel(
          channelId = cursor._id,
          name = cursor.name,
          description = cursor.description,
          createdAt = cursor.created_at.typed,
          isSubscribed = cursor.subscribed == 1
        )
      }

    override def length: Int = {
      rawCursor.getCount
    }
  }
}

trait MyChannelRecord extends TypedFields
  with ChannelRecord
  with ChannelStatusRecord

case class SettingMyChannel(
  channelId: Long,
  name: String,
  description: String,
  createdAt: Date,
  isSubscribed: Boolean
)
