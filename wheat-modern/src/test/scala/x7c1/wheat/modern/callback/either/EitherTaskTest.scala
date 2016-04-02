package x7c1.wheat.modern.callback.either

import org.scalatest.{FlatSpecLike, Matchers}

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Await

class EitherTaskTest extends FlatSpecLike with Matchers {

  val provide = EitherTask.bindLeft[SampleError]

  it can "handle right value" in {
    val tasks = for {
      x1 <- provide right 1
      x2 <- provide right "foo"
    } yield {
      x1 + x2.length
    }
    tasks run {
      case Right(result) => result shouldBe 4
      case Left(error) => fail("unexpected error")
    }
  }
  it should "stop if error occurred" in {
    val signs = ArrayBuffer[Int]()
    val tasks = for {
      x1 <- {
        signs += 111
        provide right 1
      }
      _ <- {
        signs += 222
        provide left new SampleSubError("oops!")
      }
      x2 <- {
        signs += 333
        provide right "foo"
      }
    } yield x1 + x2.length

    tasks run {
      case Left(error) =>
        signs shouldBe Seq(111, 222)
        error.message shouldBe "oops!"
      case Right(result) =>
        fail(s"unexpected result: $result")
    }
    tasks run {
      case Left(error: SampleSubError) =>
        error.decorated shouldBe "[oops!]"
      case result =>
        fail(s"unexpected result: $result")
    }
  }
  it can "await given msec" in {
    val tasks = for {
      start <- provide right System.currentTimeMillis()
      x1 <- provide right 22
      _ <- provide await (msec = 111)
      x2 <- provide right 33
      _ <- provide await (msec = 222)
    } yield {
      val elapsed = System.currentTimeMillis() - start
      elapsed.toInt -> (x1 + x2)
    }
    val either = {
      import concurrent.duration._
      Await.result(tasks.toFuture, atMost = 5.seconds)
    }
    either match {
      case Right((elapsed, sum)) =>
        sum shouldBe 55
        elapsed should be >= 333
      case Left(error) => fail("unexpected error")
    }
  }
}

class SampleError(val message: String)

class SampleSubError(x: String) extends SampleError(x) {
  def decorated = s"[$message]"
}
