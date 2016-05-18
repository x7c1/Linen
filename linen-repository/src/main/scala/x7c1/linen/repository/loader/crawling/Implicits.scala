package x7c1.linen.repository.loader.crawling

import java.util.concurrent.Executors

object Implicits {
  import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
  private lazy val pool = Executors.newCachedThreadPool()
  implicit def executor: ExecutionContextExecutor = ExecutionContext fromExecutor pool
}