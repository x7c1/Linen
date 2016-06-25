package x7c1.wheat.modern.kinds

import org.scalatest.{FlatSpecLike, Matchers}
import x7c1.wheat.modern.kinds.RichFate.ToRichFate

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

class FateTest extends FlatSpecLike with Matchers {

  val provide = FutureFate.hold[CustomContext, CustomError]
  val context = CustomContext(ExecutionContext.global)

  it can "compose by for-yield" in {
    val fate = for {
      n1 <- provide right 1
      n2 <- provide right 2
      n3 <- provide right 3
    } yield {
      n1 + n2 + n3
    }
    fate.testRun(context){
      case Left(error) => fail(error.message)
      case Right(n) => n shouldBe 6
    }

    /*
    //cannot compile
    fate.run(CustomContextBoo(ExecutionContext.global))
    // */
  }
  it should "stop when exception thrown" in {
    val fate = for {
      n1 <- provide right 1
      n2 <- provide right { throw new Exception("boo") }
      n3 <- provide right 3
    } yield {
      n1 + n2 + n3
    }
    fate.testRun(context){
      case Left(error) =>
        error.message shouldBe "boo"
      case Right(n) =>
        fail(s"invalid response: $n")
    }
  }
  it can "await given msec" in {
    val fate = for {
      start <- provide right System.currentTimeMillis()
      n1 <- provide right 1
      _  <- provide await 111.millis
      n2 <- provide right 2
      _  <- provide await 222.millis
      n3 <- provide right 3
    } yield {
      val elapsed = System.currentTimeMillis() - start
      elapsed.toInt -> (n1 + n2 + n3)
    }
    fate.testRun(context){
      case Right((elapsed, sum)) =>
        sum shouldBe 6
        elapsed should be >= 333
      case Left(error) =>
        fail(error.message)
    }
  }
}
