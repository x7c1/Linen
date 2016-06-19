package x7c1.linen.database.mixin

import android.database.Cursor
import x7c1.linen.database.struct.ChannelRecord
import x7c1.wheat.macros.database.TypedCursor
import x7c1.wheat.modern.database.Query
import x7c1.wheat.modern.database.selector.presets.CanTraverseRecord
import x7c1.wheat.modern.database.selector.{IdEndo, Identifiable, RecordReifiable}

trait ChannelsToAttachRecord extends ChannelRecord {
  def attached_source_id: Option[Long]
}

object ChannelsToAttachRecord {
  case class Key(
    sourceId: Long,
    accountId: Long
  )
  object Key {
    implicit object id extends HasKey[Key] with IdEndo[Key]
  }
  trait HasKey[A] extends Identifiable[A, Key]

  implicit object reifiable extends RecordReifiable[ChannelsToAttachRecord]{
    override def reify(cursor: Cursor) = TypedCursor[ChannelsToAttachRecord](cursor)
  }
  implicit object traverse extends CanTraverseRecord[HasKey, ChannelsToAttachRecord]{
    override def queryAbout[X: HasKey](target: X): Query = {
      val key = implicitly[HasKey[X]] toId target
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

      Query(sql, Array(
        key.sourceId.toString,
        key.accountId.toString
      ))
    }
  }
}
