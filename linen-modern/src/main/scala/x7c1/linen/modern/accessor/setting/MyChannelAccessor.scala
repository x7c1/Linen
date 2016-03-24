package x7c1.linen.modern.accessor.setting

import android.database.sqlite.SQLiteDatabase
import x7c1.linen.modern.accessor.database.ChannelRecord
import x7c1.linen.modern.struct.Channel
import x7c1.wheat.macros.database.TypedCursor

trait MyChannelAccessor {
  def accountId: Long
  def findAt(position: Int): Option[Channel]
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
