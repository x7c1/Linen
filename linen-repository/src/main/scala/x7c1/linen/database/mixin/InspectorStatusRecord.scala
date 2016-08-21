package x7c1.linen.database.mixin

import x7c1.linen.database.struct.{HasAccountId, InspectorActionRecord, InspectorSourceRecord, SourceRecord}
import x7c1.wheat.macros.database.{Query, TypedCursor}
import x7c1.wheat.modern.database.selector.presets.{CanTraverseRecord, TraverseOn}
import Query.SqlBuilder
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import x7c1.wheat.modern.database.selector.{RecordReifiable, SelectorProvidable}

trait InspectorStatusRecord
  extends InspectorActionRecord
    with InspectorSourceRecord {

  def source_title: String
}

object InspectorStatusRecord {

  implicit object reifiable extends RecordReifiable[InspectorStatusRecord] {
    override def reify(cursor: Cursor) = TypedCursor[InspectorStatusRecord](cursor)
  }

  implicit object traverse extends CanTraverseRecord[HasAccountId, InspectorStatusRecord] {
    override def queryAbout[X: HasAccountId](target: X): Query = {
      val accountId = implicitly[HasAccountId[X]] toId target
      sql"""
         |SELECT
         |  a1.action_id AS action_id,
         |  a1.action_loading_status AS action_loading_status,
         |  a1.account_id AS account_id,
         |  a1.origin_title AS origin_title,
         |  a1.origin_url AS origin_url,
         |  a1.created_at AS created_at,
         |  a2.updated_at AS updated_at,
         |  a2.discovered_source_id AS discovered_source_id,
         |  a2.latent_source_url AS latent_source_url,
         |  a2.source_loading_status AS source_loading_status,
         |  a3.title AS source_title
         |FROM
         |  inspector_actions AS a1
         |    LEFT JOIN inspector_sources AS a2
         |      ON a1.action_id = a2.action_id
         |    LEFT JOIN sources AS a3
         |      ON discovered_source_id = a3._id
         |WHERE
         |  a1.account_id = $accountId
         |ORDER BY
         |  created_at DESC
         """
    }
  }

  implicit object providable extends SelectorProvidable[InspectorStatusRecord, Selector]

  class Selector(
    protected val db: SQLiteDatabase) extends TraverseOn[HasAccountId, InspectorStatusRecord]

}
