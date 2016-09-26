package x7c1.linen.repository.inspector

import java.net.URL

import x7c1.linen.database.struct.{InspectorLoadingStatus, InspectorSourceRecord}
import x7c1.linen.repository.date.Date
import x7c1.wheat.macros.database.TypedFields
import x7c1.wheat.macros.database.TypedFields.toArgs
import x7c1.wheat.modern.database.Updatable

case class InspectorSourceStatus(
  actionId: Long,
  latentUrl: URL,
  loadingStatus: InspectorLoadingStatus,
  discoveredSourceId: Option[Long],
  updatedAt: Date
)

object InspectorSourceStatus {

  import InspectorSourceRecord.column

  implicit object updatable extends Updatable[InspectorSourceStatus] {
    override def tableName = InspectorSourceRecord.table

    override def toContentValues(target: InspectorSourceStatus) =
      TypedFields.toContentValues(
        column.source_loading_status -> target.loadingStatus,
        column.discovered_source_id -> target.discoveredSourceId,
        column.updated_at -> target.updatedAt
      )

    override def where(target: InspectorSourceStatus) = toArgs(
      column.action_id -> target.actionId,
      column.latent_source_url -> target.latentUrl.toExternalForm
    )
  }

}