package x7c1.linen.database.struct

import android.database.Cursor
import x7c1.linen.repository.date.Date
import x7c1.wheat.macros.database.TypedFields.toArgs
import x7c1.wheat.macros.database.{TypedCursor, TypedFields}
import x7c1.wheat.modern.database.Insertable
import x7c1.wheat.modern.database.selector.presets.{CanFindRecord, DefaultProvidable}
import x7c1.wheat.modern.database.selector.{Identifiable, RecordReifiable}


trait AccountRecord extends TypedFields {
  def _id: Long
  def nickname: String
  def biography: String
  def created_at: Int --> Date
}
object AccountRecord {
  def table: String = "accounts"
  def column = TypedFields.expose[AccountRecord]

  implicit object providable extends DefaultProvidable[AccountIdentifiable, AccountRecord]

  implicit object reifiable extends RecordReifiable[AccountRecord]{
    override def reify(cursor: Cursor) = TypedCursor[AccountRecord](cursor)
  }
  implicit object findable extends CanFindRecord.Where[AccountIdentifiable, AccountRecord](table){
    override def where[X](id: Long) = toArgs(column._id -> id)
  }
}

trait AccountIdentifiable[A] extends Identifiable[A, Long]

case class AccountParts(
  nickname: String,
  biography: String,
  createdAt: Date
)

object AccountParts {
  private def column = TypedFields.expose[AccountRecord]

  implicit object insertable extends Insertable[AccountParts]{
    override def tableName = AccountRecord.table
    override def toContentValues(target: AccountParts) =
      TypedFields toContentValues (
        column.nickname -> target.nickname,
        column.biography -> target.biography,
        column.created_at -> target.createdAt
      )
  }
}
