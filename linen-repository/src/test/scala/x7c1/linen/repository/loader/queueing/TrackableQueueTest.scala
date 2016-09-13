package x7c1.linen.repository.loader.queueing

import java.net.URL

import org.scalatest.{FlatSpecLike, Matchers}
import x7c1.linen.repository.loader.crawling.CrawlerContext
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.kinds.Fate
import x7c1.wheat.modern.queue.map.TrackableQueue

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.{Await, Future, Promise}

class TrackableQueueTest extends FlatSpecLike with Matchers {
  behavior of TrackableQueue.getClass.getSimpleName

  it can "enqueue" in {
    val runner = new SampleRunner
    val queue = TrackableQueue[CrawlerContext, UrlTraverserError, SampleUrl, Unit](
      valueQueue = UrlEnclosureQueue(),
      callee = runner.startAction
    )
    val links = Seq(
      "http://example.com/1",
      "http://example.com/2",
      "http://example.com/3"
    )
    val fates = links map { target =>
      SampleUrl(new URL(target))
    } map { url =>
      queue enqueue url
    }
    import RichFate._

    fates.toParallel.testRun(CrawlerContext) {
      case Left(es) =>
        es foreach (Log error _.message)
        fail("unexpected errors")
      case Right(_) =>
        runner.buffer.exists(_.toExternalForm == "http://example.com/1") shouldBe true
        runner.buffer.exists(_.toExternalForm == "http://example.com/2") shouldBe true
        runner.buffer.exists(_.toExternalForm == "http://example.com/3") shouldBe true
    }
  }
}

case class SampleUrl(
  override val raw: URL) extends UrlEnclosure

class SampleRunner {
  val buffer = ArrayBuffer[URL]()

  val startAction: UrlReceiver = UrlReceiver {
    case url: SampleUrl =>
      buffer.append(url.raw)
  }
}

object RichFate {

  implicit class ToRichFate[X, L, R](fate: Fate[X, L, R]) {
    def toFuture(context: X): Future[Either[L, R]] = {
      val promise = Promise[Either[L, R]]()
      fate.run(context) { either =>
        try promise trySuccess either
        catch {
          case e: Throwable => promise tryFailure e
        }
      }
      promise.future
    }

    def testRun(context: X)(f: Either[L, R] => Unit): Unit = {
      import concurrent.duration._
      val either = Await.result(toFuture(context), atMost = 5.seconds)
      f(either)
    }
  }

}
