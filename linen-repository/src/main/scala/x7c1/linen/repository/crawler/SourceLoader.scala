package x7c1.linen.repository.crawler

import java.io.{InputStreamReader, BufferedInputStream}
import java.net.{HttpURLConnection, URL}

import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.{SyndFeed, SyndEntry}
import com.google.code.rome.android.repackaged.com.sun.syndication.io.SyndFeedInput
import x7c1.linen.database.struct.EntryParts
import x7c1.linen.repository.date.Date
import x7c1.linen.repository.entry.EntryUrl
import x7c1.wheat.modern.callback.CallbackTask
import x7c1.wheat.modern.callback.CallbackTask.task
import x7c1.wheat.modern.callback.TaskProvider.using

import scala.concurrent.Future

trait SourceLoader {
  def loadSource(source: InspectedSource): Future[LoadedSource]
}

object RemoteSourceLoader extends SourceLoader {
  import Implicits._

  override def loadSource(source: InspectedSource): Future[LoadedSource] = {
    import collection.JavaConverters._

    Future(source.feedUrl).map(loadRawFeed).flatMap(_.toFuture) map { feed =>
      val entries = feed.getEntries.asScala map { case x: SyndEntry => x }
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
        author = Option(entry.getAuthor) getOrElse "",
        url = EntryUrl(url),
        createdAt = Date(published)
      ) catch {
      case e: Exception => Left(Abort(e))
    }
  }
}
