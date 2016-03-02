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
    override def query(id: Unit) = select
    override def fromCursor(raw: Cursor) =
      TypedCursor[PresetAccountColumn](raw) moveToHead { cursor =>
        PresetAccount(
          accountId = cursor.account_id,
          tagLabel = cursor.tag_label
        )
      }
  }
  def select = new Query(
    s"""SELECT
       |  t2.tag_label as tag_label,
       |  t1.account_id as account_id
       |FROM account_tag_map AS t1
       |  LEFT JOIN account_tags AS t2 ON
       |    t1.account_tag_id = t2.account_tag_id
       |WHERE t2.tag_label = 'preset'
       |ORDER BY t1.account_id ASC
    """.stripMargin
  )

}

trait PresetAccountColumn extends TypedFields {
  def account_id: Long
  def tag_label: String
}
