package x7c1.linen.modern.accessor.preset

import android.database.Cursor
import x7c1.linen.modern.accessor.{Query, SingleQuerySelectable}
import x7c1.wheat.macros.database.{TypedCursor, TypedFields}


case class PresetAccount(
  accountId: Long,
  tagLabel: String
)

object PresetAccount {
  implicit object selectable extends SingleQuerySelectable[PresetAccount, Unit]{
    override def query(id: Unit): Query = new Query(sql, Array())
    override def fromCursor(raw: Cursor): Option[PresetAccount] =
      TypedCursor[PresetAccountColumn](raw) moveToHead { cursor =>
        PresetAccount(
          accountId = cursor.account_id,
          tagLabel = cursor.tag_label
        )
      }
  }
  val sql =
    s"""
       |SELECT
       |  a1._id as account_id,
       |  a3.tag_label as tag_label
       |FROM accounts AS a1
       |  INNER JOIN account_tag_map AS a2 ON
       |    a1._id = a2.account_id
       |  INNER JOIN account_tags AS a3 ON
       |    a2.account_tag_id = a3.account_tag_id AND
       |    a3.tag_label = 'preset'
       |  ORDER BY a1._id ASC
       |  LIMIT 1
     """.stripMargin
}

trait PresetAccountColumn extends TypedFields {
  def account_id: Long
  def tag_label: String
}
