package x7c1.linen.repository.loader.crawling

import java.util.Timer
import java.util.concurrent.Executors

import x7c1.wheat.modern.kinds.FutureFate.{HasContext, HasTimer}
import x7c1.wheat.modern.kinds.{FateRunner, FutureFate}

import scala.concurrent.ExecutionContext

trait CrawlerContext {
  private val executionContext: ExecutionContext = {
    val pool = Executors.newCachedThreadPool()
    ExecutionContext fromExecutor pool
  }
  private val timer: Timer = new Timer
}

object CrawlerContext extends CrawlerContext {
  implicit object hasContext extends HasContext[CrawlerContext]{
    override def value = _.executionContext
  }
  implicit object hasTimer extends HasTimer[CrawlerContext]{
    override def value = _.timer
  }
}

object CrawlerFate {
  def run[R](f: => R): FateRunner[CrawlerFateError, R] = {
    FutureFate.hold[CrawlerContext, CrawlerFateError].right(f) run CrawlerContext
  }
}
