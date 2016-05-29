package x7c1.linen.database.struct

import android.database.Cursor
import x7c1.linen.repository.date.Date
import x7c1.wheat.macros.database.TypedFields.toArgs
import x7c1.wheat.macros.database.{TypedCursor, TypedFields}
import x7c1.wheat.modern.database.Insertable
import x7c1.wheat.modern.database.selector.RecordReifiable
import x7c1.wheat.modern.database.selector.presets.CanFindRecord.Where
import x7c1.wheat.modern.database.selector.presets.DefaultProvidable


trait account_tag_map extends TypedFields {
  def account_id: Long
  def account_tag_id: Long
  def created_at:  Int --> Date
}

object account_tag_map {

  def table = "account_tag_map"

  def column = TypedFields.expose[account_tag_map]

  implicit object reifiable extends RecordReifiable[account_tag_map]{
    override def reify(cursor: Cursor) = TypedCursor[account_tag_map](cursor)
  }
  implicit object providable
    extends DefaultProvidable[HasAccountId, account_tag_map]

  implicit object findable extends Where[HasAccountId, account_tag_map](table){
    override def where[X](id: Long) = toArgs(
      column.account_id -> id
    )
  }
  implicit object accountTagId extends HasAccountTagId[account_tag_map]{
    override def toId = _.account_tag_id
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
