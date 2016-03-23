package x7c1.linen.modern.accessor

import android.database.sqlite.SQLiteDatabase
import x7c1.linen.modern.struct.{Channel, Date}
import x7c1.wheat.macros.database.{TypedFields, TypedCursor}

trait ChannelAccessor {
  def accountId: Long
  def findAt(position: Int): Option[Channel]
  def length: Int
}

object ChannelAccessor {
  def create(db: SQLiteDatabase, accountId: Long): ChannelAccessor = {
    new ChannelAccessorImpl(db, accountId)
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
    private lazy val cursor = TypedCursor[ChannelRecord](rawCursor)

    override def findAt(position: Int) =
      cursor.moveToFind(position){
        Channel(
          channelId = cursor._id,
          name = cursor.name,
          description = cursor.description,
          createdAt = cursor.created_at.typed
        )
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

trait ChannelRecord extends TypedFields {
  def _id: Long
  def name: String
  def description: String
  def account_id: Long
  def created_at: Int --> Date
}

object ChannelParts {
  implicit object insertable extends Insertable[ChannelParts] {

    override def tableName = "channels"

    override def toContentValues(parts: ChannelParts) = {
      val column = TypedFields.expose[ChannelRecord]
      TypedFields toContentValues (
        column.name -> parts.name,
        column.description -> parts.description,
        column.account_id -> parts.accountId,
        column.created_at -> parts.createdAt
      )
    }
  }
}
