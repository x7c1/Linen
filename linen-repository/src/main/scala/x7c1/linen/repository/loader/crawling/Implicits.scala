package x7c1.linen.repository.loader.crawling

import java.util
import java.util.Timer
import java.util.concurrent.Executors

import x7c1.wheat.modern.features.HasSharedInstance
import x7c1.wheat.modern.kinds.FutureFate.HasContext
import x7c1.wheat.modern.kinds.{FateRunner, FutureFate}

trait CrawlerContext

object CrawlerContext extends CrawlerContext {
  import scala.concurrent.ExecutionContext

  private val executor = {
    val pool = Executors.newCachedThreadPool()
    ExecutionContext fromExecutor pool
  }
  implicit object context extends HasContext[CrawlerContext]{
    override def value = _ => executor
  }
  implicit object timer extends HasSharedInstance[CrawlerContext, Timer]{
    override val instance = new util.Timer
  }
}

object CrawlerFate {
  def run[R](f: => R): FateRunner[CrawlerFateError, R] = {
    FutureFate.hold[CrawlerContext, CrawlerFateError].right(f) run CrawlerContext
  }
}
