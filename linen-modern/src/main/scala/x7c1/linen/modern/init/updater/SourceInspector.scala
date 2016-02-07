package x7c1.linen.modern.init.updater

import java.io.{BufferedInputStream, InputStreamReader}
import java.net.{HttpURLConnection, URL}

import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndEntry
import com.google.code.rome.android.repackaged.com.sun.syndication.io.SyndFeedInput
import x7c1.linen.modern.accessor.{EntryParts, LinenOpenHelper, SourceRecordColumn}
import x7c1.linen.modern.struct.Date
import x7c1.wheat.modern.callback.CallbackTask
import x7c1.wheat.modern.callback.CallbackTask.{task, using}

import scala.concurrent.Future

object SourceInspector {
  def apply(helper: LinenOpenHelper ): SourceInspector = new SourceInspector(helper)
}

class SourceInspector private (helper: LinenOpenHelper){

  def inspectSource(sourceId: Long): Either[SourceInspectorError, InspectedSource] =
    helper.readable.find[SourceRecordColumn](sourceId) match {
      case Left(e) => Left(SqlError(e))
      case Right(None) => Left(SourceNotFound(sourceId))
      case Right(Some(source)) => Right(InspectedSource(sourceId, new URL(source.url)))
    }

  def loadEntries(source: InspectedSource): Future[LoadedEntries] = {
    val callback = loadRawEntries(source.feedUrl)
    val entries = callback.toFuture

    import LinenService.Implicits._
    entries map (_ map convertEntry(source.sourceId)) map (new LoadedEntries(_))
  }
  private def loadRawEntries(feedUrl: URL): CallbackTask[Seq[SyndEntry]] = {
    import scala.collection.JavaConversions._
    for {
      connection <- task {
        val connection = feedUrl.openConnection().asInstanceOf[HttpURLConnection]
        connection setRequestMethod "GET"
        connection
      }
      stream <- using(new BufferedInputStream(connection.getInputStream))
      reader <- using(new InputStreamReader(stream))
    } yield {
      val feed = new SyndFeedInput().build(reader)
      feed.getEntries map { case x: SyndEntry => x }
    }
  }

  private def convertEntry(sourceId: Long)(entry: SyndEntry): Either[InvalidEntry, EntryParts] = {
    try for {
      url <- (Option(entry.getLink) toRight EmptyUrl()).right
      published <- (Option(entry.getPublishedDate) toRight EmptyPublishedDate()).right
    } yield EntryParts(
      sourceId = sourceId,
      title = Option(entry.getTitle) getOrElse "",
      content = Option(entry.getDescription.getValue) getOrElse "",
      url = url,
      createdAt = Date(published)
    ) catch {
      case e: Exception => Left(Abort(e))
    }
  }

}

case class InspectedSource(
  sourceId: Long,
  feedUrl: URL
)

class LoadedEntries(entries: Seq[Either[InvalidEntry, EntryParts]]){
  lazy val validEntries = entries collect { case Right(x) => x }
  lazy val invalidEntries = entries collect { case Left(x) => x }
}
