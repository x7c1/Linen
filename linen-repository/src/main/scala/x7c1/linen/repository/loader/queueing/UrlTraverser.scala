package x7c1.linen.repository.loader.queueing

import x7c1.linen.repository.loader.crawling.CrawlerContext
import x7c1.wheat.modern.kinds.Fate
import x7c1.wheat.modern.queue.map.{TrackableQueue, ValueQueue}

trait UrlTraverser[A <: UrlEnclosure, B <: UrlTraverserOutput] {
  def startLoading(url: A): Fate[CrawlerContext, UrlTraverserError, B]
}

private class UrlTraverserImpl[A <: UrlEnclosure, B <: UrlTraverserOutput](
  callee: A => B ) extends UrlTraverser[A, B] {

  private val queue = TrackableQueue[CrawlerContext, UrlTraverserError, A, B](
    valueQueue = UrlEnclosureQueue(),
    callee = callee
  )

  override def startLoading(url: A) = {
    queue.enqueue(url)
  }
}

object UrlEnclosureQueue {
  def apply[A <: UrlEnclosure](): ValueQueue[A] = {
    ValueQueue.toDistribute(getGroupKey = _.raw.getHost)
  }
}

trait UrlTraverserOutput
