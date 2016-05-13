package x7c1.linen.database.mixin

import android.database.Cursor
import x7c1.linen.database.struct.{AccountIdentifiable, ChannelStatusRecord, ChannelRecord}
import x7c1.wheat.macros.database.{TypedCursor, TypedFields}
import x7c1.wheat.modern.database.Query
import x7c1.wheat.modern.database.selector.RecordReifiable
import x7c1.wheat.modern.database.selector.presets.CanTraverseRecord

trait MyChannelRecord extends TypedFields
  with ChannelRecord
  with ChannelStatusRecord

object MyChannelRecord {
  def column = TypedFields.expose[MyChannelRecord]

  implicit object reifiable extends RecordReifiable[MyChannelRecord]{
    override def reify(cursor: Cursor) = TypedCursor[MyChannelRecord](cursor)
  }
  implicit object traversable extends CanTraverseRecord[AccountIdentifiable, MyChannelRecord]{
    override def query[X: AccountIdentifiable](target: X): Query = {
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
          |WHERE c1.account_id = ?
          |ORDER BY c1._id DESC""".stripMargin

      val id = implicitly[AccountIdentifiable[X]] toId target
      new Query(sql, Array(id.toString, id.toString))
    }
  }
}