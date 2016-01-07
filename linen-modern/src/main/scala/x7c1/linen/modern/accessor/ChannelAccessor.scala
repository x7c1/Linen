package x7c1.linen.modern.accessor

import android.database.sqlite.SQLiteDatabase
import x7c1.linen.modern.struct.Channel

trait ChannelAccessor {
  def findAt(position: Int): Option[Channel]
  def length: Int
  def findFirstId(): Option[Long]
}

object ChannelAccessor {
  def create(db: SQLiteDatabase, accountId: Long): ChannelAccessor = {
    new ChannelAccessorImpl(db)
  }
  private class ChannelAccessorImpl(db: SQLiteDatabase) extends ChannelAccessor {
    override def findAt(position: Int) = {
      ???
    }
    override def length: Int = ???

    override def findFirstId(): Option[Long] = {
      val cursor = db.rawQuery("SELECT * FROM channels LIMIT 1", Array())
      cursor.moveToFirst() match {
        case true =>
          val idIndex = cursor getColumnIndex "_id"
          Some(cursor getLong idIndex)
        case _ =>
          None
      }
    }
  }

}
