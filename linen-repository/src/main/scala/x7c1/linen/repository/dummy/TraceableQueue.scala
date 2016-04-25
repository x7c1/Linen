package x7c1.linen.repository.dummy

import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.repository.crawler.{InspectedSource, SourceDequeueEvent, SourceLoader, SourceUpdaterQueue, UpdatedSource}

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
  def enqueueSource(source: InspectedSource)(implicit x: ExecutionContext): Future[UpdatedSource] = {
    val promise = Promise[UpdatedSource]()
    map(source) = promise
    enqueue(source)
    promise.future
  }
  private def onSourceDequeue(event: SourceDequeueEvent): Unit = {
    val promise = map remove event.inspected getOrElse {
      throw new IllegalArgumentException
    }
    promise tryComplete event.updated
  }
}
