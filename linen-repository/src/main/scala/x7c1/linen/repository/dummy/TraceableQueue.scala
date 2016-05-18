package x7c1.linen.repository.dummy

import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.repository.loader.crawling.{InspectedSource, SourceLoader, UpdatedSource, SourceDequeueEvent, SourceUpdaterQueue}
import x7c1.wheat.macros.logger.Log

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future, Promise}

class TraceableQueue(
  helper: DatabaseHelper,
  sourceLoader: SourceLoader) extends SourceUpdaterQueue {

  private lazy val map = mutable.Map[InspectedSource, Promise[UpdatedSource]]()

  private lazy val queue = SourceUpdaterQueue(helper, sourceLoader, onSourceDequeue)

  override def enqueue(source: InspectedSource)(implicit x: ExecutionContext): Unit = {
    queue.enqueue(source)
  }
  def enqueueSource(source: InspectedSource)(implicit x: ExecutionContext): Future[UpdatedSource] =
    synchronized {
      map get source match {
        case None =>
          val promise = Promise[UpdatedSource]()
          map(source) = promise
          enqueue(source)
          promise.future
        case Some(promise) =>
          promise.future
      }
    }

  private def onSourceDequeue(event: SourceDequeueEvent): Unit =
    synchronized {
      map remove event.inspected match {
        case Some(promise) =>
          promise tryComplete event.updated
        case None =>
          Log warn {
            val id = event.inspected.sourceId
            val title = event.inspected.title
            s"source already dequeued: id:$id, $title"
          }
      }
    }
}
