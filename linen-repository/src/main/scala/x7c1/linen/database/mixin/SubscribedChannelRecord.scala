package x7c1.linen.database.mixin

import android.database.Cursor
import x7c1.linen.database.struct.{ChannelRecord, ChannelStatusRecord, HasAccountId}
import x7c1.wheat.macros.database.{TypedCursor, TypedFields}
import x7c1.wheat.modern.database.Query
import x7c1.wheat.modern.database.selector.RecordReifiable
import x7c1.wheat.modern.database.selector.presets.CanTraverseRecord

trait SubscribedChannelRecord extends TypedFields
  with ChannelRecord
  with ChannelStatusRecord

object SubscribedChannelRecord {
  def column = TypedFields.expose[SubscribedChannelRecord]

  implicit object reifiable extends RecordReifiable[SubscribedChannelRecord]{
    override def reify(cursor: Cursor) = TypedCursor[SubscribedChannelRecord](cursor)
  }
  implicit object traversable extends CanTraverseRecord[HasAccountId, SubscribedChannelRecord]{
    override def query[X: HasAccountId](target: X): Query = {
      val sql =
        """SELECT
          | channel_id,
          | c1.account_id as account_id,
          | c1.channel_rank as channel_rank,
          | c1.updated_at as updated_at,
          | name
          |FROM channel_statuses AS c1
          | INNER JOIN channels AS c2
          |   ON c1.channel_id = c2._id AND c1.account_id = ?
          |WHERE c1.subscribed = 1
          |ORDER BY c1.channel_rank ASC, c1.updated_at DESC
        """.stripMargin

      val id = implicitly[HasAccountId[X]] toId target
      Query(sql, Array(id.toString))
    }
  }
}
