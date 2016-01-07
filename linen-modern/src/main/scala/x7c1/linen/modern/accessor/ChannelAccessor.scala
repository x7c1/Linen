package x7c1.linen.modern.accessor

import android.content.ContentValues
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import x7c1.linen.modern.struct.{Channel, Date}

trait ChannelAccessor {
  def findAt(position: Int): Option[Channel]
  def length: Int
  def findFirstId(): Option[Long] = {
    findAt(0).map(_.channelId)
  }
}

object ChannelAccessor {
  def create(db: SQLiteDatabase, accountId: Long): ChannelAccessor = {
    new ChannelAccessorImpl(db, accountId)
  }
  private class ChannelAccessorImpl(
    db: SQLiteDatabase, accountId: Long) extends ChannelAccessor {

    private lazy val idIndex = cursor getColumnIndex "channel_id"
    private lazy val nameIndex = cursor getColumnIndex "name"
    private lazy val descriptionIndex = cursor getColumnIndex "description"
    private lazy val createdAtIndex = cursor getColumnIndex "created_at"

    private lazy val cursor = {
      val sql1 =
        """SELECT
          | _id as channel_id,
          | name,
          | description,
          | created_at
          |FROM channels
          |WHERE account_id = ? ORDER BY _id DESC""".stripMargin

      db.rawQuery(sql1, Array(accountId.toString))
    }
    override def findAt(position: Int) = {
      if (cursor moveToPosition position){
        Some apply Channel(
          channelId = cursor getLong idIndex,
          name = cursor getString nameIndex,
          description = cursor getString descriptionIndex,
          createdAt = Date(cursor getInt createdAtIndex)
        )
      } else {
        None
      }
    }
    override def length: Int = {
      cursor.getCount
    }
  }
}

case class ChannelParts(
  accountId: Long,
  name: String,
  description: String,
  createdAt: Date
)

class ChannelsTable private (db: SQLiteDatabase) {
  def insert(parts: ChannelParts): Either[SQLException, Long] = {
    val values = toValues(parts)
    try {
      Right apply db.insertOrThrow("channels", null, values)
    } catch {
      case e: SQLException => Left(e)
    }
  }
  def toValues(parts: ChannelParts): ContentValues = {
    val values = new ContentValues()
    values.put("name", parts.name)
    values.put("description", parts.description)
    values.put("account_id", parts.accountId.toString)
    values.put("created_at", parts.createdAt.timestamp.toString)
    values
  }
}

object ChannelsTable {
  def apply(db: SQLiteDatabase): ChannelsTable = {
    new ChannelsTable(db)
  }
}
