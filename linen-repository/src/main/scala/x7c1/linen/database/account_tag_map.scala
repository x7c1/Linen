package x7c1.linen.database

import android.database.Cursor
import x7c1.linen.domain.Date
import x7c1.wheat.macros.database.{TypedCursor, TypedFields}


trait account_tag_map extends TypedFields {
  def account_id: Long
  def account_tag_id: Long
  def created_at:  Int --> Date
}

object account_tag_map {

  def table = "account_tag_map"

  implicit object selectable extends SingleWhere[account_tag_map, Long](table){
    override def where(id: Long) = Seq("account_id" -> id.toString)
    override def fromCursor(raw: Cursor) = {
      TypedCursor[account_tag_map](raw).freezeAt(0)
    }
  }
}

case class AccountTagMapParts(
  accountId: Long,
  accountTagId: Long,
  createdAt: Date
)

object AccountTagMapParts {
  implicit object insertable extends Insertable[AccountTagMapParts]{
    override def tableName = account_tag_map.table
    override def toContentValues(target: AccountTagMapParts) = {
      val column = TypedFields.expose[account_tag_map]
      TypedFields toContentValues (
        column.account_id -> target.accountId,
        column.account_tag_id -> target.accountTagId,
        column.created_at -> target.createdAt
        )
    }
  }
}
