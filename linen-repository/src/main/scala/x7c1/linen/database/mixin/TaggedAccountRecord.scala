package x7c1.linen.database.mixin

import android.database.Cursor
import x7c1.linen.database.struct.AccountTagLabel
import x7c1.wheat.macros.database.{Query, TypedCursor, TypedFields}
import x7c1.wheat.modern.database.selector.RecordReifiable

trait TaggedAccountRecord extends TypedFields {
  def account_id: Long
  def tag_label: String
}

object TaggedAccountRecord {
  def select(label: AccountTagLabel): Query = Query(
    sql =
      s"""SELECT
         |  t2.tag_label as tag_label,
         |  t1.account_id as account_id
         |FROM account_tag_map AS t1
         |  LEFT JOIN account_tags AS t2 ON
         |    t1.account_tag_id = t2.account_tag_id
         |WHERE t2.tag_label = ?
         |ORDER BY t1.account_id ASC
      """.stripMargin,

    selectionArgs = Array(label.text)
  )
  implicit object reifiable extends RecordReifiable[TaggedAccountRecord]{
    override def reify(cursor: Cursor) = TypedCursor[TaggedAccountRecord](cursor)
  }
}
