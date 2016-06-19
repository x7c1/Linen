package x7c1.linen.database.struct

import android.database.Cursor
import x7c1.linen.repository.date.Date
import x7c1.wheat.macros.database.TypedFields.toArgs
import x7c1.wheat.macros.database.{TypedCursor, TypedFields}
import x7c1.wheat.modern.database.selector.presets.CanFindRecord.Where
import x7c1.wheat.modern.database.selector.presets.DefaultProvidable
import x7c1.wheat.modern.database.selector.{IdEndo, Identifiable, RecordReifiable}
import x7c1.wheat.modern.database.{HasTable, Insertable}


trait AccountRecord extends TypedFields {
  def _id: Long
  def nickname: String
  def biography: String
  def created_at: Int --> Date
}
object AccountRecord {
  def table: String = "accounts"

  def column = TypedFields.expose[AccountRecord]

  implicit object hasTable extends HasTable.Where[AccountRecord](table)

  implicit object providable extends DefaultProvidable[HasAccountId, AccountRecord]

  implicit object reifiable extends RecordReifiable[AccountRecord]{
    override def reify(cursor: Cursor) = TypedCursor[AccountRecord](cursor)
  }
  implicit object findable extends Where[HasAccountId, AccountRecord]{
    override def where[X](id: Long) = toArgs(column._id -> id)
  }
}

trait HasAccountId[A] extends Identifiable[A, Long]

object HasAccountId {
  implicit object id extends HasAccountId[Long] with IdEndo[Long]
}

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
