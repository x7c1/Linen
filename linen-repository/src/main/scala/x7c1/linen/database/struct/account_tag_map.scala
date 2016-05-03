package x7c1.linen.database.struct

import android.database.Cursor
import x7c1.linen.repository.date.Date
import x7c1.wheat.macros.database.{TypedCursor, TypedFields}
import x7c1.wheat.modern.database.RecordFindable.Where
import x7c1.wheat.modern.database.presets.DefaultProvidable
import x7c1.wheat.modern.database.{Insertable, RecordReifiable}


trait account_tag_map extends TypedFields {
  def account_id: Long
  def account_tag_id: Long
  def created_at:  Int --> Date
}

object account_tag_map {

  def table = "account_tag_map"

  implicit object reifiable extends RecordReifiable[account_tag_map]{
    override def reify(cursor: Cursor) = TypedCursor[account_tag_map](cursor)
  }
  implicit object providable
    extends DefaultProvidable[AccountIdentifiable, account_tag_map]

  implicit object findable extends Where[AccountIdentifiable, account_tag_map](table){
    override def where[X](id: Long) = {
      Seq("account_id" -> id.toString)
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
