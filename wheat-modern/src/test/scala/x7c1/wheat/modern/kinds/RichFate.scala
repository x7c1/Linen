package x7c1.wheat.modern.kinds

import concurrent.duration._
import scala.concurrent.{Await, Future, Promise}

object RichFate {
  implicit class ToRichFate[X, L, R](fate: Fate[X, L, R]){
    def toFuture(context: X): Future[Either[L, R]] = {
      val promise = Promise[Either[L, R]]()
      fate.run(context){ either =>
        try promise trySuccess either
        catch { case e: Throwable => promise tryFailure e }
      }
      promise.future
    }
    def testRun(context: X)(f: Either[L, R] => Unit): Unit = {
      val either = Await.result(toFuture(context), atMost = 5.seconds)
      f(either)
    }
  }
}
