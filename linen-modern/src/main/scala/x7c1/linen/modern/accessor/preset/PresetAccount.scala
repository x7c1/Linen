package x7c1.linen.modern.accessor.preset

import android.database.Cursor
import x7c1.linen.database.{AccountTagLabel, ClientLabel, PresetLabel, Query, ZeroAritySingle}
import x7c1.linen.domain.AccountIdentifiable
import x7c1.linen.modern.accessor.preset.TaggedAccountRecord.select
import x7c1.wheat.macros.database.{TypedCursor, TypedFields}


case class PresetAccount(accountId: Long) extends AccountIdentifiable

object PresetAccount {
  implicit object selectable extends ZeroAritySingle[PresetAccount](select(PresetLabel)){
    override def fromCursor(cursor: Cursor) = {
      TypedCursor[TaggedAccountRecord](cursor) moveToHead reify
    }
  }
  private def reify(record: TaggedAccountRecord) = {
    PresetAccount(record.account_id)
  }
}

trait TaggedAccountRecord extends TypedFields {
  def account_id: Long
  def tag_label: String
}
object TaggedAccountRecord {
  def select(label: AccountTagLabel) = new Query(
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
}

case class ClientAccount(accountId: Long) extends AccountIdentifiable

object ClientAccount {
  implicit object selectable extends ZeroAritySingle[ClientAccount](select(ClientLabel)){
    override def fromCursor(cursor: Cursor) = {
      TypedCursor[TaggedAccountRecord](cursor) moveToHead reify
    }
  }
  private def reify(record: TaggedAccountRecord) = {
    ClientAccount(record.account_id)
  }
}
