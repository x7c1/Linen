package x7c1.linen.repository.crawler

import java.net.URL

import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.database.struct.SourceRecord

object SourceInspector {
  def apply(helper: DatabaseHelper ): SourceInspector = new SourceInspector(helper)
}

class SourceInspector private (helper: DatabaseHelper){

  def inspectSource(sourceId: Long): Either[SourceInspectorError, InspectedSource] =
    for {
      source <- getSource(sourceId).right
      entry <- getEntry(sourceId).right
    } yield {
      InspectedSource(source, entry)
    }

  private def getSource(sourceId: Long) =
    helper.readable.find[SourceRecord] by sourceId via {
      case Left(e) => Left(SqlError(e))
      case Right(None) => Left(SourceNotFound(sourceId))
      case Right(Some(source)) => Right(source)
    }

  private def getEntry(sourceId: Long) =
    helper.readable.find[LatestEntry] by sourceId via {
      case Left(e) => Left(SqlError(e))
      case Right(x) => Right(x)
    }

}

class InspectedSource(
  val sourceId: Long,
  val title: String,
  val description: String,
  val feedUrl: URL,
  val latestEntry: Option[LatestEntry]
)
object InspectedSource {
  def apply(source: SourceRecord, entry: Option[LatestEntry]): InspectedSource = {
    new InspectedSource(
      sourceId = source._id,
      title = source.title,
      description = source.description,
      feedUrl = new URL(source.url),
      latestEntry = entry
    )
  }
}
