package x7c1.linen.modern.init.updater

import java.io.{BufferedInputStream, InputStreamReader}
import java.net.{HttpURLConnection, URL}

import android.app.Service
import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndEntry
import com.google.code.rome.android.repackaged.com.sun.syndication.io.SyndFeedInput
import x7c1.linen.glue.service.ServiceControl
import x7c1.linen.modern.accessor.{EntryParts, LinenOpenHelper, SourceRecordColumn}
import x7c1.linen.modern.struct.Date
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.callback.CallbackTask
import x7c1.wheat.modern.callback.CallbackTask.using

import scalaz.{\/, \/-}

class SourceLoader(
  service: Service with ServiceControl,
  helper: LinenOpenHelper,
  startId: Int,
  sourceId: Long){

  def start(): Unit = {
    Log error s"[init] source-id: $sourceId"

    helper.readable.find[SourceRecordColumn](sourceId) match {
      case Right(Some(source)) =>
        Log info source.title

        try loadEntries(source)(insertEntries)
        catch {
          case e: Exception => Log error e.getMessage
        }

      case Right(none) =>
      case Left(exception) =>
    }
    service stopSelfResult startId
  }

  private def insertEntries(entries: Seq[EntryNotLoaded \/ EntryParts]): Unit = {

    val loadedEntries = entries collect { case \/-(entry) => entry }
    val notifier = new UpdaterServiceNotifier(service, loadedEntries.length)
    loadedEntries.zipWithIndex foreach {
      case (entry, index) =>
        Log info entry.title
        helper.writableDatabase insert entry
        notifier.notifyProgress(index)
    }
    notifier.notifyDone()
  }

  private def loadEntries
    (source: SourceRecordColumn): CallbackTask[Seq[EntryNotLoaded \/ EntryParts]] = {

    val sourceUrl = source.url
    Log info s"[init] $sourceUrl"

    val feedUrl = new URL(sourceUrl)
    val connection = feedUrl.openConnection().asInstanceOf[HttpURLConnection]
    connection setRequestMethod "GET"

    Log info s"code:${connection.getResponseCode}"

    import scala.collection.JavaConversions._
    for {
      stream <- using(new BufferedInputStream(connection.getInputStream))
      reader <- using(new InputStreamReader(stream))
    } yield {
      val feed = new SyndFeedInput().build(reader)

      Log error feed.getTitle
      Log info feed.getDescription

      feed.getEntries map { case entry: SyndEntry =>
        convertEntry(source)(entry)
      }
    }
  }

  private def convertEntry
    (source: SourceRecordColumn)(entry: SyndEntry): EntryNotLoaded \/ EntryParts = {
    import scalaz.\/.left
    import scalaz.syntax.std.option._

    try for {
      url <- Option(entry.getLink) \/> EmptyUrl()
      published <- Option(entry.getPublishedDate) \/> EmptyPublishedDate()
    } yield EntryParts(
        sourceId = source._id,
        title = Option(entry.getTitle) getOrElse "",
        content = Option(entry.getDescription.getValue) getOrElse "",
        url = url,
        createdAt = Date(published)
      ) catch {
      case e: Exception => left apply Abort(e)
    }
  }
}
