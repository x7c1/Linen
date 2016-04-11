package x7c1.linen.modern.accessor.setting

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import x7c1.linen.modern.accessor.Query
import x7c1.linen.modern.accessor.database.ChannelRecord
import x7c1.wheat.macros.database.TypedCursor
import x7c1.wheat.modern.sequence.Sequence

trait ChannelsToAttachAccessor extends Sequence[ChannelToAttach]

object ChannelsToAttachAccessor {
  def create(
    db: SQLiteDatabase,
    accountId: Long,
    sourceId: Long ): Either[Exception, ChannelsToAttachAccessor] = {

    try {
      val query = createQuery(accountId, sourceId)
      val raw = db.rawQuery(query.sql, query.selectionArgs)
      Right apply new ChannelsToAttachImpl(raw)
    } catch {
      case e: Exception => Left(e)
    }
  }
  def createQuery(accountId: Long, sourceId: Long): Query = {
    val sql =
      """SELECT
        | c1._id AS _id,
        | c1.name AS name,
        | c2.source_id AS attached_source_id
        |FROM channels AS c1
        | LEFT JOIN channel_source_map AS c2 ON
        |   c1._id = c2.channel_id AND
        |   c2.source_id = ?
        |WHERE
        | c1.account_id = ?
        |""".stripMargin

    new Query(sql, Array(
      sourceId.toString,
      accountId.toString
    ))
  }
}

trait ChannelsToAttachRecord extends ChannelRecord {
  def attached_source_id: Option[Long]
}

private class ChannelsToAttachImpl(rawCursor: Cursor) extends ChannelsToAttachAccessor {

  private lazy val cursor = TypedCursor[ChannelsToAttachRecord](rawCursor)

  override def length = rawCursor.getCount

  override def findAt(position: Int) =
    (cursor moveToFind position){
      new ChannelToAttach(
        channelId = cursor._id,
        channelName = cursor.name,
        isAttached = cursor.attached_source_id.nonEmpty
      )
    }
}

class ChannelToAttach (
  val channelId: Long,
  val channelName: String,
  val isAttached: Boolean
)
