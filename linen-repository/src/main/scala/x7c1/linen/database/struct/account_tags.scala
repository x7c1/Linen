package x7c1.linen.database.struct

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import x7c1.linen.repository.date.Date
import x7c1.wheat.macros.database.TypedFields.toArgs
import x7c1.wheat.macros.database.{TypedCursor, TypedFields}
import x7c1.wheat.modern.database.HasTable
import x7c1.wheat.modern.database.selector.presets.CanFindRecord.Where
import x7c1.wheat.modern.database.selector.presets.{FindBy, FindByTag}
import x7c1.wheat.modern.database.selector.{Identifiable, RecordReifiable, SelectorProvidable}

trait account_tags extends TypedFields {
  def account_tag_id: Long
  def tag_label: String
  def created_at: Int --> Date
}

object account_tags {

  def table = "account_tags"

  def column = TypedFields.expose[account_tags]

  implicit object hasTable extends HasTable.Where[account_tags](table)

  implicit object reifiable extends RecordReifiable[account_tags]{
    override def reify(cursor: Cursor) = TypedCursor[account_tags](cursor)
  }
  implicit object labelFindable extends Where[HasAccountTagLabel, account_tags]{
    override def where[X](label: AccountTagLabel) = toArgs(column.tag_label -> label.text)
  }
  implicit object idFindable extends Where[HasAccountTagId, account_tags]{
    override def where[X](id: Long) = toArgs(column.account_tag_id -> id)
  }
  implicit object providable extends SelectorProvidable[account_tags, Selector]

  class Selector(val db: SQLiteDatabase)
    extends FindBy[HasAccountTagId, account_tags]
      with FindByTag[HasAccountTagLabel, account_tags]
}
trait HasAccountTagId[A] extends Identifiable[A, Long]

trait HasAccountTagLabel[A] extends Identifiable[A, AccountTagLabel]
