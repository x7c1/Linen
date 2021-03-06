package x7c1.linen.repository.loader.crawling

import java.net.URL

import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.database.struct.{HasSourceId, SourceRecord}
import x7c1.linen.repository.loader.crawling.SourceInspectorError.{SourceNotFound, SqlError}

object SourceInspector {
  def apply(helper: DatabaseHelper ): SourceInspector = new SourceInspector(helper)
}

class SourceInspector private (helper: DatabaseHelper){

  def inspectSource[A: HasSourceId](sourceId: A): Either[SourceInspectorError, InspectedSource] =
    for {
      source <- getSource(sourceId).right
      entry <- getEntry(sourceId).right
    } yield {
      InspectedSource(source, entry)
    }

  private def getSource[A: HasSourceId](sourceId: A) =
    helper.selectorOf[SourceRecord] findBy sourceId matches {
      case Left(e) => Left(SqlError(e))
      case Right(None) => Left(SourceNotFound(sourceId))
      case Right(Some(source)) => Right(source)
    }

  private def getEntry[A: HasSourceId](sourceId: A) =
    helper.selectorOf[LatestEntry] findBy sourceId matches {
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
){
  override def toString = s"sourceId:$sourceId,title:$title,$feedUrl"
}
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
