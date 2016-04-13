package x7c1.linen.modern.init.updater

import java.lang.System.currentTimeMillis

import android.app.Service
import android.database.sqlite.SQLiteConstraintException
import x7c1.linen.database.control.LinenOpenHelper
import x7c1.linen.glue.service.ServiceControl
import x7c1.wheat.modern.formatter.ThrowableFormatter
import ThrowableFormatter.format
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.patch.TaskAsync.after

import scala.collection.mutable

class SourceUpdaterQueue(
  service: Service with ServiceControl,
  helper: LinenOpenHelper){

  private lazy val inspector = SourceInspector(helper)
  private lazy val queueMap = new SourceQueueMap

  def enqueue(source: InspectedSource): Unit = synchronized {
    Log info s"[init] source:$source"
    val host = source.feedUrl.getHost
    if (queueMap has host){
      queueMap enqueue source
    } else {
      queueMap enqueue source
      update(source)
    }
  }
  private def update(source: InspectedSource): Unit = {
    Log info s"[init] source:$source"
    import x7c1.linen.modern.init.updater.LinenService.Implicits._

    val start = currentTimeMillis()
    def elapsed() = currentTimeMillis() - start

    val future = inspector.loadSource(source) map { loadedSource =>
      Log info s"[loaded] msec:${elapsed()}, source:$source"

      if (loadedSource isModifiedFrom source){
        updateSource(loadedSource)
      }
      insertEntries(loadedSource)
    }
    future onFailure {
      case e => Log error format(e)(s"[error] ${source.feedUrl}")
    }
    future onComplete { _ =>
      val host = source.feedUrl.getHost
      val nextSource = this synchronized {
        queueMap dequeue host
        Log info s"[inserted] msec:${elapsed()}, left:${queueMap length host}, feed:${source.feedUrl}"
        queueMap headOption host
      }
      nextSource match {
        case Some(next) => after(msec = 1000){ update(next) }
        case None => Log info s"[done] host:$host"
      }
    }
  }
  private def updateSource(source: LoadedSource): Unit = {
    helper.writable update source match {
      case Left(error) => Log error error.getMessage
      case Right(0) => Log info s"not updated: ${source.title}"
      case Right(_) => Log info s"updated: ${source.title}"
    }
  }
  private def insertEntries(source: LoadedSource): Unit = {
    val loadedEntries = source.validEntries
//    val notifier = new UpdaterServiceNotifier(service, loadedEntries.length)
    loadedEntries.zipWithIndex foreach {
      case (entry, index) =>
        helper.writable insert entry match {
          case Left(e: SQLiteConstraintException) =>
            // nop, entry already exists
          case Left(e) =>
            Log error s"$index,${entry.url.host},${e.getMessage}"
          case Right(b) =>
            Log debug s"$index,${entry.url.host},${entry.title}"
        }

//        notifier.notifyProgress(index)
    }
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
