package x7c1.linen.modern.init.updater

import java.io.{BufferedInputStream, InputStreamReader}
import java.net.{HttpURLConnection, URL}

import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.{SyndEntry, SyndFeed}
import com.google.code.rome.android.repackaged.com.sun.syndication.io.SyndFeedInput
import x7c1.linen.modern.accessor.database.SourceRecord
import x7c1.linen.modern.accessor.{EntryParts, EntryUrl, LinenOpenHelper}
import x7c1.linen.modern.struct.Date
import x7c1.wheat.modern.callback.CallbackTask
import x7c1.wheat.modern.callback.CallbackTask.{task, using}

import scala.concurrent.Future

object SourceInspector {
  def apply(helper: LinenOpenHelper ): SourceInspector = new SourceInspector(helper)
}

class SourceInspector private (helper: LinenOpenHelper){

  def inspectSource(sourceId: Long): Either[SourceInspectorError, InspectedSource] =
    helper.readable.find[SourceRecord].by(sourceId) match {
      case Left(e) => Left(SqlError(e))
      case Right(None) => Left(SourceNotFound(sourceId))
      case Right(Some(source)) => Right(InspectedSource(source))
    }

  def loadSource(source: InspectedSource): Future[LoadedSource] = {
    import LinenService.Implicits.executor

    import scala.collection.JavaConversions._

    Future(source.feedUrl).map(loadRawFeed).flatMap(_.toFuture) map { feed =>
      val entries = feed.getEntries map { case x: SyndEntry => x }
      new LoadedSource(
        sourceId = source.sourceId,
        title = Option(feed.getTitle) getOrElse "",
        description = Option(feed.getDescription) getOrElse "",
        entries = entries map convertEntry(source.sourceId)
      )
    }
  }
  private def loadRawFeed(feedUrl: URL): CallbackTask[SyndFeed] = {
    for {
      connection <- task {
        val connection = feedUrl.openConnection().asInstanceOf[HttpURLConnection]
        connection setRequestMethod "GET"
        connection
      }
      stream <- using(new BufferedInputStream(connection.getInputStream))
      reader <- using(new InputStreamReader(stream))
    } yield {
      new SyndFeedInput().build(reader)
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
      url = EntryUrl(url),
      createdAt = Date(published)
    ) catch {
      case e: Exception => Left(Abort(e))
    }
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
