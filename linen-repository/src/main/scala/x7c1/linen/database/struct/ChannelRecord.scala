package x7c1.linen.database.struct

import android.database.Cursor
import x7c1.linen.repository.date.Date
import x7c1.wheat.macros.database.TypedFields.toArgs
import x7c1.wheat.macros.database.{TypedCursor, TypedFields}
import x7c1.wheat.modern.database.selector.presets.CanFindRecord.Where
import x7c1.wheat.modern.database.selector.presets.DefaultProvidable
import x7c1.wheat.modern.database.selector.{IdEndo, Identifiable, RecordReifiable}
import x7c1.wheat.modern.database.{Deletable, HasTable, Insertable}

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

  implicit object hasTable extends HasTable.Where[ChannelRecord](table)

  implicit object reifiable extends RecordReifiable[ChannelRecord]{
    override def reify(cursor: Cursor) = TypedCursor[ChannelRecord](cursor)
  }
  implicit object findable extends Where[HasChannelId, ChannelRecord]{
    override def where[X](id: Long) = toArgs(
      column._id -> id
    )
  }
  implicit object fromName extends Where[HasNamedChannelKey, ChannelRecord]{
    override def where[X](key: NamedChannelKey) = toArgs(
      column.account_id -> key.accountId,
      column.name -> key.channelName
    )
  }
  implicit object providable extends DefaultProvidable[HasChannelId, ChannelRecord]

  implicit object id extends HasChannelId[ChannelRecord]{
    override def toId = _._id
  }
}

trait HasChannelId[A] extends Identifiable[A, Long]

object HasChannelId {
  implicit object id extends HasChannelId[Long] with IdEndo[Long]
}
trait HasNamedChannelKey[A] extends Identifiable[A, NamedChannelKey]

case class NamedChannelKey(
  accountId: Long,
  channelName: String
)
object NamedChannelKey {
  implicit object id extends HasNamedChannelKey[NamedChannelKey]
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
  override def where(target: A) = toArgs(
    column._id -> f(target)
  )
}
