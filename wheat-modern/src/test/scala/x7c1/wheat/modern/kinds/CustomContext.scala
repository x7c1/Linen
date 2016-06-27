package x7c1.wheat.modern.kinds

import java.util
import java.util.Timer

import x7c1.wheat.modern.kinds.FutureFate.{HasContext, HasTimer}

import scala.concurrent.ExecutionContext

object CustomContext extends CustomContext {
  implicit object hasContext extends HasContext[CustomContext]{
    override def value = _.context
  }
  implicit object hasTimer extends HasTimer[CustomContext]{
    override def value = _.timer
  }
}

trait CustomContext {
  val context: ExecutionContext = ExecutionContext.global
  val timer: Timer = new util.Timer()
}

case class CustomContextBoo(context: ExecutionContext)
