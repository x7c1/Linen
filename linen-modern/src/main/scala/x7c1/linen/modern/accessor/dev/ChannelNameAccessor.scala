package x7c1.linen.modern.accessor.dev

import android.database.sqlite.SQLiteDatabase
import x7c1.linen.modern.accessor.ChannelRecord
import x7c1.wheat.macros.database.TypedCursor

trait ChannelNameAccessor {
  def findAt(position: Int): Option[ChannelName]
  def length: Int
  def allNames: Seq[ChannelName] = (0 to length - 1) flatMap findAt
}

object ChannelNameAccessor {
  def apply(db: SQLiteDatabase): ChannelNameAccessor = {
    new ChannelNameAccessorImpl(db)
  }
}

private class ChannelNameAccessorImpl(db: SQLiteDatabase) extends ChannelNameAccessor {

  private lazy val rawCursor = {
    val sql = s"SELECT _id, name FROM channels ORDER BY _id DESC"
    db.rawQuery(sql, Array())
  }
  private lazy val cursor = TypedCursor[ChannelRecord](rawCursor)

  override def findAt(position: Int) =
    cursor.moveToFind(position){
      ChannelName(
        channelId = cursor._id,
        name = cursor.name
      )
    }

  override def length = rawCursor.getCount
}

case class ChannelName(
  channelId: Long,
  name: String
)
