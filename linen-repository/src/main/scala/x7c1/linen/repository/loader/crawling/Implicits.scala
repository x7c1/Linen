package x7c1.linen.repository.loader.crawling

import java.util.concurrent.Executors

import x7c1.wheat.modern.kinds.FutureFate.HasContext

object Implicits {
  import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
  private lazy val pool = Executors.newCachedThreadPool()
  implicit def executor: ExecutionContextExecutor = ExecutionContext fromExecutor pool
}

trait CrawlerContext

object CrawlerContext extends CrawlerContext {
  implicit object context extends HasContext[CrawlerContext]{
    override def value = _ => Implicits.executor
  }
}
