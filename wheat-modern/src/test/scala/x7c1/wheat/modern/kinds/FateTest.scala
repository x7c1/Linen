package x7c1.wheat.modern.kinds

import org.scalatest.{FlatSpecLike, Matchers}
import x7c1.wheat.modern.kinds.RichFate.ToRichFate

import scala.concurrent.ExecutionContext

class FateTest extends FlatSpecLike with Matchers {

  val holder = FutureFate.hold[CustomContext, CustomError]
  val context = CustomContext(ExecutionContext.global)

  it can "compose by for-yield" in {
    val fate = for {
      n1 <- holder(Right(1))
      n2 <- holder(Right(2))
      n3 <- holder(Right(3))
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
  it can "stop when error occurs" in {
    val fate = for {
      n1 <- holder(Right(1))
      n2 <- holder{ throw new Exception("boo") }
      n3 <- holder(Right(3))
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
}

