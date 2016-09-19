package x7c1.linen.repository.loader.queueing

import x7c1.linen.repository.loader.crawling.CrawlerContext
import x7c1.wheat.modern.kinds.Fate
import x7c1.wheat.modern.queue.map.{TrackableQueue, GroupingQueue}

trait UrlTraverser[A <: UrlEnclosure, B] {
  def startLoading(url: A): Fate[CrawlerContext, UrlTraverserError, B]
}

object UrlTraverser {
  def apply[A <: UrlEnclosure, B](callee: A => B): UrlTraverser[A, B] = {
    new UrlTraverserImpl(callee)
  }
}

private class UrlTraverserImpl[A <: UrlEnclosure, B](
  callee: A => B) extends UrlTraverser[A, B] {

  private val queue = TrackableQueue[CrawlerContext, UrlTraverserError, A, B](
    createQueue = () => UrlEnclosureQueue(),
    callee = callee
  )

  override def startLoading(url: A) = {
    queue.enqueue(url)
  }
}

object UrlEnclosureQueue {
  def apply[A <: UrlEnclosure](): GroupingQueue[A] = {
    GroupingQueue.groupBy(getGroupKey = _.raw.getHost)
  }
}
