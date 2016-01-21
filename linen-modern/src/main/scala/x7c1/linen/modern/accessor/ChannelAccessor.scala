package x7c1.linen.modern.accessor

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import x7c1.linen.modern.struct.{Channel, Date}
import x7c1.wheat.macros.database.TypedCursor

trait ChannelAccessor {
  def accountId: Long
  def findAt(position: Int): Option[Channel]
  def length: Int
}

object ChannelAccessor {
  def create(db: SQLiteDatabase, accountId: Long): ChannelAccessor = {
    new ChannelAccessorImpl(db, accountId)
  }
  def findCurrentChannelId(db: SQLiteDatabase, accountId: Long): Option[Long] = {
    ChannelAccessor.create(db, accountId) findAt 0 map (_.channelId)
  }

  private class ChannelAccessorImpl(
    db: SQLiteDatabase,
    override val accountId: Long) extends ChannelAccessor {

    private lazy val rawCursor = {
      val sql1 =
        """SELECT
          | _id,
          | name,
          | description,
          | created_at
          |FROM channels
          |WHERE account_id = ? ORDER BY _id DESC""".stripMargin

      db.rawQuery(sql1, Array(accountId.toString))
    }
    private lazy val cursor = TypedCursor[ChannelRecordColumn](rawCursor)

    override def findAt(position: Int) = {
      if (cursor moveTo position){
        Some apply Channel(
          channelId = cursor._id,
          name = cursor.name,
          description = cursor.description,
          createdAt = cursor.created_at.typed
        )
      } else {
        None
      }
    }
    override def length: Int = {
      rawCursor.getCount
    }
  }
}

case class ChannelParts(
  accountId: Long,
  name: String,
  description: String,
  createdAt: Date
)

trait ChannelRecordColumn extends TypedCursor {
  def _id: Long
  def name: String
  def description: String
  def created_at: Int --> Date
}

object ChannelParts {
  implicit object insertable extends Insertable[ChannelParts] {
    override def tableName = "channels"
    override def toContentValues(parts: ChannelParts) = {
      val values = new ContentValues()
      values.put("name", parts.name)
      values.put("description", parts.description)
      values.put("account_id", parts.accountId: java.lang.Long)
      values.put("created_at", parts.createdAt.timestamp: java.lang.Integer)
      values
    }
  }
}
