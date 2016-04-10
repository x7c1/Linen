package x7c1.linen.modern.accessor.setting

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import x7c1.linen.modern.accessor.Query
import x7c1.wheat.macros.database.TypedCursor
import x7c1.wheat.modern.sequence.Sequence

trait ChannelsToAttachAccessor extends Sequence[MyChannel]

object ChannelsToAttachAccessor {
  def create(
    db: SQLiteDatabase,
    accountId: Long,
    channelIdToExclude: Long): Either[Exception, ChannelsToAttachAccessor] = {

    try {
      val query = createQuery(accountId, channelIdToExclude)
      val raw = db.rawQuery(query.sql, query.selectionArgs)
      Right apply new ChannelsToAttachImpl(raw, accountId)
    } catch {
      case e: Exception => Left(e)
    }
  }

  def createQuery(accountId: Long, channelIdToExclude: Long): Query = {
    val sql =
      """SELECT
        | _id,
        | name,
        | description,
        | IFNULL(c2.subscribed, 0) AS subscribed,
        | c1.created_at AS created_at
        |FROM channels AS c1
        | LEFT JOIN channel_statuses AS c2
        |   ON c1._id = c2.channel_id AND c2.account_id = ?
        |WHERE
        | c1.account_id = ? AND
        | c1._id != ?
        |ORDER BY c1._id DESC""".stripMargin

    new Query(sql, Array(
      accountId.toString,
      accountId.toString,
      channelIdToExclude.toString
    ))
  }
}

private class ChannelsToAttachImpl(
  rawCursor: Cursor, accountId: Long ) extends ChannelsToAttachAccessor {

  private lazy val cursor = TypedCursor[MyChannelRecord](rawCursor)

  override def length = rawCursor.getCount

  override def findAt(position: Int) =
    (cursor moveToFind position){
      MyChannel(
        channelId = cursor._id,
        name = cursor.name,
        description = cursor.description,
        createdAt = cursor.created_at.typed,
        isSubscribed = cursor.subscribed == 1
      )
    }
}
