package x7c1.linen.database.struct

import android.database.Cursor
import x7c1.linen.repository.date.Date
import x7c1.wheat.macros.database.{TypedCursor, TypedFields}
import x7c1.wheat.modern.database.Insertable
import x7c1.wheat.modern.database.selector.{RecordReifiable, Identifiable}
import x7c1.wheat.modern.database.selector.presets.{CanFindRecord, DefaultProvidable}


trait AccountRecord extends TypedFields {
  def nickname: String
  def biography: String
  def created_at: Int --> Date
}
object AccountRecord {
  def table: String = "accounts"

  implicit object providable extends DefaultProvidable[AccountIdentifiable, AccountRecord]

  implicit object reifiable extends RecordReifiable[AccountRecord]{
    override def reify(cursor: Cursor) = TypedCursor[AccountRecord](cursor)
  }
  implicit object findable extends CanFindRecord.Where[AccountIdentifiable, AccountRecord](table){
    override def where[X](id: Long) = Seq("_id" -> id.toString)
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
