package x7c1.linen.database.struct

import android.database.Cursor
import x7c1.linen.repository.date.Date
import x7c1.wheat.macros.database.TypedFields.toSelectionArgs
import x7c1.wheat.macros.database.{TypedCursor, TypedFields}
import x7c1.wheat.modern.database.selector.presets.CanFindRecord.Where
import x7c1.wheat.modern.database.selector.{IdEndo, Identifiable, RecordReifiable}
import x7c1.wheat.modern.database.{Deletable, Insertable}

trait ChannelRecord extends TypedFields {
  def _id: Long
  def name: String
  def description: String
  def account_id: Long
  def created_at: Int --> Date
}

object ChannelRecord {
  def table: String = "channels"
  def column: ChannelRecord = TypedFields.expose[ChannelRecord]

  implicit object reifiable extends RecordReifiable[ChannelRecord]{
    override def reify(cursor: Cursor) = TypedCursor[ChannelRecord](cursor)
  }
  implicit object findable extends Where[ChannelIdentifiable, ChannelRecord](table){
    override def where[X](id: Long) = toSelectionArgs(
      column._id -> id
    )
  }
  implicit object fromName extends Where[NamedChannelIdentifiable, ChannelRecord](table){
    override def where[X](key: NamedChannelKey) = toSelectionArgs(
      column.account_id -> key.accountId,
      column.name -> key.channelName
    )
  }
}

trait ChannelIdentifiable[A] extends Identifiable[A, Long]

object ChannelIdentifiable {
  implicit object id extends ChannelIdentifiable[Long] with IdEndo[Long]
}
trait NamedChannelIdentifiable[A] extends Identifiable[A, NamedChannelKey]

case class NamedChannelKey(
  accountId: Long,
  channelName: String
)
object NamedChannelKey {
  implicit object id extends NamedChannelIdentifiable[NamedChannelKey]
    with IdEndo[NamedChannelKey]
}

case class ChannelParts(
  accountId: Long,
  name: String,
  description: String,
  createdAt: Date
)

object ChannelParts {
  import ChannelRecord.column

  implicit object insertable extends Insertable[ChannelParts] {
    override def tableName = ChannelRecord.table
    override def toContentValues(parts: ChannelParts) = {
      TypedFields toContentValues (
        column.name -> parts.name,
        column.description -> parts.description,
        column.account_id -> parts.accountId,
        column.created_at -> parts.createdAt
      )
    }
  }
}

class ChannelDeletable[A](f: A => Long) extends Deletable[A]{
  import ChannelRecord.column

  override def tableName: String = ChannelRecord.table
  override def where(target: A) = toSelectionArgs(
    column._id -> f(target)
  )
}
