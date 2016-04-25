package x7c1.linen.repository.crawler

import java.net.URL

import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.database.struct.SourceRecord

object SourceInspector {
  def apply(helper: DatabaseHelper ): SourceInspector = new SourceInspector(helper)
}

class SourceInspector private (helper: DatabaseHelper){

  def inspectSource(sourceId: Long): Either[SourceInspectorError, InspectedSource] =
    helper.readable.find[SourceRecord].by(sourceId) via {
      case Left(e) => Left(SqlError(e))
      case Right(None) => Left(SourceNotFound(sourceId))
      case Right(Some(source)) => Right(InspectedSource(source))
    }
}

case class InspectedSource(
  sourceId: Long,
  title: String,
  description: String,
  feedUrl: URL
)
object InspectedSource {
  def apply(source: SourceRecord): InspectedSource = {
    InspectedSource(
      sourceId = source._id,
      title = source.title,
      description = source.description,
      feedUrl = new URL(source.url)
    )
  }
}
