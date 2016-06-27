package x7c1.linen.repository.loader.crawling

import x7c1.linen.database.control.DatabaseHelper
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.kinds.{Fate, FutureFate}

import scala.collection.mutable
import scala.concurrent.Promise

class TraceableQueue(
  helper: DatabaseHelper,
  sourceLoader: SourceLoader) extends SourceUpdaterQueue {

  private lazy val map = mutable.Map[InspectedSource, Promise[UpdatedSource]]()

  private lazy val queue = SourceUpdaterQueue(helper, sourceLoader, onSourceDequeue)

  override def enqueue(source: InspectedSource) = {
    queue.enqueue(source)
  }
  def enqueueSource(source: InspectedSource): Fate[CrawlerContext, SourceQueueError, UpdatedSource] = {
    val promise = synchronized {
      map get source match {
        case None =>
          val promise = Promise[UpdatedSource]()
          map(source) = promise
          enqueue(source) run CrawlerContext atLeft {
            Log error _.detail
          }
          promise
        case Some(existent) =>
          existent
      }
    }
    FutureFate.hold[CrawlerContext, SourceQueueError] fromPromise promise
  }

  private def onSourceDequeue(event: SourceDequeueEvent): Unit =
    synchronized {
      map remove event.inspected match {
        case Some(promise) =>
          event.updated match {
            case Left(e) => promise failure e.cause
            case Right(source) => promise success source
          }
        case None =>
          Log warn {
            val id = event.inspected.sourceId
            val title = event.inspected.title
            s"source already dequeued: id:$id, $title"
          }
      }
    }

}
