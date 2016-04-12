package x7c1.linen.database

import android.database.Cursor
import x7c1.linen.domain.Date
import x7c1.wheat.macros.database.{TypedCursor, TypedFields}

trait account_tags extends TypedFields {
  def account_tag_id: Long
  def tag_label: String
  def created_at: Int --> Date
}

object account_tags {

  def table = "account_tags"

  implicit def tagSelectable[A <: AccountTagLabel]: SingleWhere[account_tags, A] =
    new SingleWhere[account_tags, A](table){
      override def where(label: A) = Seq("tag_label" -> label.text)
      override def fromCursor(raw: Cursor) = TypedCursor[account_tags](raw) freezeAt 0
    }

  implicit object idSelectable extends SingleWhere[account_tags, Long](table){
    override def where(id: Long) = Seq("account_tag_id" -> id.toString)
    override def fromCursor(raw: Cursor) = TypedCursor[account_tags](raw) freezeAt 0
  }

}
