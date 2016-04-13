package x7c1.linen.database.struct

import x7c1.linen.repository.date.Date
import x7c1.wheat.macros.database.TypedFields
import x7c1.wheat.modern.database.Insertable


trait AccountRecord extends TypedFields {
  def nickname: String
  def biography: String
  def created_at: Int --> Date
}
object AccountRecord {
  def table: String = "accounts"
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
