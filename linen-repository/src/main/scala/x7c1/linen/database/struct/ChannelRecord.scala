package x7c1.linen.database.struct

import android.database.Cursor
import x7c1.linen.repository.date.Date
import x7c1.wheat.macros.database.{TypedCursor, TypedFields}
import x7c1.wheat.modern.database.selector.presets.CanFindRecord
import x7c1.wheat.modern.database.selector.{RecordReifiable, Identifiable}
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
  implicit object findable extends CanFindRecord.Where[ChannelIdentifiable, ChannelRecord](table){
    override def where[X](id: Long) = Seq("_id" -> id.toString)
  }
}

trait ChannelIdentifiable[A] extends Identifiable[A, Long]

object ChannelIdentifiable {
  implicit object id extends ChannelIdentifiable[Long]{
    override def idOf(target: Long): Long = target
  }
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
  override def tableName: String = ChannelRecord.table
  override def where(target: A): Seq[(String, String)] = Seq(
    "_id" -> f(target).toString
  )
}
