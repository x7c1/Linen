package x7c1.linen.database.mixin

import android.database.Cursor
import x7c1.linen.database.struct.{AccountIdentifiable, ChannelRecord, ChannelStatusRecord}
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
  implicit object traversable extends CanTraverseRecord[AccountIdentifiable, SubscribedChannelRecord]{
    override def query[X: AccountIdentifiable](target: X): Query = {
      // todo: sort by channel order
      val sql =
        """SELECT
          | channel_id,
          | name
          |FROM channel_statuses AS c1
          | INNER JOIN channels AS c2
          |   ON c1.channel_id = c2._id AND c1.account_id = ?
          |WHERE c1.subscribed = 1
        """.stripMargin

      val id = implicitly[AccountIdentifiable[X]] toId target
      new Query(sql, Array(id.toString))
    }
  }
}