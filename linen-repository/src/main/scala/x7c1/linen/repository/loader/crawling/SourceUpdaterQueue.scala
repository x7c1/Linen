package x7c1.linen.repository.loader.crawling

import java.lang.System.currentTimeMillis

import android.database.sqlite.SQLiteConstraintException
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.database.struct.{EntryParts, RetrievedSourceMarkParts}
import x7c1.linen.repository.date.Date
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.formatter.ThrowableFormatter.format
import x7c1.wheat.modern.kinds.{Fate, FutureFate}

import scala.collection.mutable
import scala.concurrent.duration.DurationInt

object SourceUpdaterQueue {
  def apply(
    helper: DatabaseHelper,
    sourceLoader: SourceLoader,
    onSourceDequeue: SourceDequeueEvent => Unit = _ => {}): SourceUpdaterQueue = {

    new SourceUpdaterQueueImpl(helper, sourceLoader, onSourceDequeue)
  }
}

trait SourceUpdaterQueue {
  def enqueue(source: InspectedSource): Fate[CrawlerContext, SourceQueueError, Unit]
}

private class SourceUpdaterQueueImpl(
  helper: DatabaseHelper,
  sourceLoader: SourceLoader,
  onSourceDequeue: SourceDequeueEvent => Unit) extends SourceUpdaterQueue {

  private lazy val queueMap = new SourceQueueMap

  override def enqueue(source: InspectedSource) = {
    val provide = FutureFate.hold[CrawlerContext, SourceQueueError]
    provide right synchronized {
      val hostExists = queueMap has source.feedUrl.getHost
      queueMap enqueue source
      hostExists
    } flatMap {
      case true => provide.empty
      case false => update(source)
    }
  }
  private def update(source: InspectedSource): Fate[CrawlerContext, SourceQueueError, Unit] = {
    val start = currentTimeMillis()
    def elapsed() = currentTimeMillis() - start

    val fate = sourceLoader.loadSource(source) map { loadedSource =>
      Log info s"[loaded] msec:${elapsed()}, source:$source, entries(${loadedSource.validEntries.length})"

      if (loadedSource isModifiedFrom source){
        updateSource(loadedSource)
      }
      val entries = source.latestEntry match {
        case Some(latest) =>
          loadedSource.validEntries filter newerThan(latest)
        case None =>
          loadedSource.validEntries
      }
      val insertedEntries = insertEntries(entries map toEntryParts(source.sourceId))
      UpdatedSource(loadedSource, insertedEntries)
    } transform { result =>
      val host = source.feedUrl.getHost
      val nextSource = this synchronized {
        queueMap dequeue host
        Log info {
          val remains = queueMap length host
          s"[dequeued] msec:${elapsed()}, remains:$remains, feed:${source.feedUrl}"
        }
        queueMap headOption host
      }
      try {
        val either = result.left map SourceQueueError.LoadingError
        onSourceDequeue(SourceDequeueEvent(source, either))
      } catch {
        case e: Exception => Log error format(e){"[failed]"}
      }
      Right(nextSource)
    }
    val provide = FutureFate.hold[CrawlerContext, SourceQueueError]
    fate flatMap {
      case Some(next) => provide await 750.millis flatMap { _ =>
        update(next)
      }
      case None => provide right {
        Log info s"[done] host:${source.feedUrl.getHost}"
      }
    }
  }
  private def toEntryParts(sourceId: Long): LoadedEntry => EntryParts =
    entry => EntryParts(
      sourceId = sourceId,
      title = entry.title,
      content = entry.content,
      author = entry.author,
      url = entry.url,
      createdAt = entry.createdAt
    )

  private def newerThan(latest: LatestEntry) = (parts: LoadedEntry) => {
    (parts.createdAt.timestamp >= latest.createdAt.timestamp) &&
      (parts.url.raw != latest.entryUrl)
  }
  private def updateSource(source: LoadedSource): Unit = {
    helper.writable update source match {
      case Left(error) => Log error error.getMessage
      case Right(0) => Log info s"not updated: ${source.title}"
      case Right(_) => Log info s"updated: ${source.title}"
    }
  }
  private def insertEntries(entries: Seq[EntryParts]): Seq[(Long, EntryParts)] = {

//    val notifier = new UpdaterServiceNotifier(service, loadedEntries.length)
    val marks = entries.zipWithIndex flatMap {
      case (entry, index) =>
        helper.writable insert entry match {
          case Left(e: SQLiteConstraintException) =>
            // nop, entry already exists
            None
          case Left(e) =>
            Log error s"$index,${entry.url.host},${e.getMessage}"
            None
          case Right(entryId) =>
            Log debug s"$index,${entry.url.host},${entry.title} (by ${entry.author})"
            Some(entryId -> entry)
        }

//        notifier.notifyProgress(index)
    }
    marks.headOption foreach {
      case (entryId, entry) =>
        helper.writable replace RetrievedSourceMarkParts(
          sourceId = entry.sourceId,
          latestEntryId = entryId,
          latestEntryCreatedAt = entry.createdAt,
          updatedAt = Date.current()
        ) match {
          case Left(error) =>
            Log error format(error){"[failed]"}
          case Right(b) =>
            Log info s"[done] marked:id:$b"
        }
    }
    marks
//    notifier.notifyDone()
  }

}

private class SourceQueueMap {
  private val map = mutable.Map[String, mutable.Queue[InspectedSource]]()

  def has(host: String): Boolean = {
    map get host exists (_.nonEmpty)
  }
  def length(host: String): Int = {
    map get host map (_.length) getOrElse 0
  }
  def enqueue(source: InspectedSource): Unit = synchronized {
    val host = source.feedUrl.getHost
    map.getOrElseUpdate(host, mutable.Queue()) enqueue source
  }
  def dequeue(host: String): Option[InspectedSource] = synchronized {
    map get host match {
      case Some(queue) if queue.isEmpty =>
        map remove host
        None
      case Some(queue) =>
        val source = queue.dequeue()
        if (queue.isEmpty){
          map remove host
        }
        Some(source)
      case None =>
        None
    }
  }
  def headOption(host: String): Option[InspectedSource] = {
    map.get(host).flatMap(_.headOption)
  }
}

case class SourceDequeueEvent(
  inspected: InspectedSource,
  updated: Either[SourceQueueError, UpdatedSource]
)

case class UpdatedSource(
  source: LoadedSource,
  insertedEntries: Seq[(Long, EntryParts)]
)
