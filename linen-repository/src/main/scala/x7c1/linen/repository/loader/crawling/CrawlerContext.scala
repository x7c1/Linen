package x7c1.linen.repository.loader.crawling

import java.util.Timer
import java.util.concurrent.Executors

import x7c1.wheat.modern.fate.FateProvider.HasContext
import x7c1.wheat.modern.fate.FutureFate
import x7c1.wheat.modern.fate.FutureFate.HasTimer
import x7c1.wheat.modern.kinds.FateRunner

import scala.concurrent.ExecutionContext

trait CrawlerContext {
  private val executionContext = {
    val pool = Executors.newCachedThreadPool()
    ExecutionContext fromExecutor pool
  }
  private val timer = new Timer
}

object CrawlerContext extends CrawlerContext {
  implicit object hasContext extends HasContext[CrawlerContext]{
    override def instance = _.executionContext
  }
  implicit object hasTimer extends HasTimer[CrawlerContext]{
    override def instance = _.timer
  }
}

object CrawlerFate {
  def run[R](f: => R): FateRunner[CrawlerFateError, R] = {
    FutureFate.hold[CrawlerContext, CrawlerFateError].right(f) run CrawlerContext
  }
}
