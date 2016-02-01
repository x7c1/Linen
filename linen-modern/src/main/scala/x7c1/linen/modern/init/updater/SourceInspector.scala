package x7c1.linen.modern.init.updater

import java.io.{BufferedInputStream, InputStreamReader}
import java.net.{HttpURLConnection, URL}

import android.app.Service
import android.database.SQLException
import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndEntry
import com.google.code.rome.android.repackaged.com.sun.syndication.io.SyndFeedInput
import x7c1.linen.glue.service.ServiceControl
import x7c1.linen.modern.accessor.{EntryParts, LinenOpenHelper, SourceRecordColumn}
import x7c1.linen.modern.struct.Date
import x7c1.wheat.modern.callback.CallbackTask
import x7c1.wheat.modern.callback.CallbackTask.{task, using}

import scala.concurrent.Future
import scalaz.\/

class SourceInspector(
  service: Service with ServiceControl,
  helper: LinenOpenHelper ){

  def findFeedUrl(sourceId: Long): Either[SQLException, Option[URL]] =
    helper.readable.find[SourceRecordColumn](sourceId).right map {
      _ map (source => new URL(source.url))
    }

  def loadEntries(sourceId: Long)(feedUrl: URL): Future[Seq[InvalidEntry \/ EntryParts]] = {
    val callback = loadRawEntries(feedUrl)
    val entries = callback.toFuture

    import LinenService.Implicits._
    entries map (_ map convertEntry(sourceId))
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
  private def convertEntry(sourceId: Long)(entry: SyndEntry): InvalidEntry \/ EntryParts = {
    import scalaz.\/.left
    import scalaz.syntax.std.option._

    try for {
      url <- Option(entry.getLink) \/> EmptyUrl()
      published <- Option(entry.getPublishedDate) \/> EmptyPublishedDate()
    } yield EntryParts(
      sourceId = sourceId,
      title = Option(entry.getTitle) getOrElse "",
      content = Option(entry.getDescription.getValue) getOrElse "",
      url = url,
      createdAt = Date(published)
    ) catch {
      case e: Exception => left apply Abort(e)
    }
  }

}
