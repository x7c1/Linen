package x7c1.wheat.modern.kinds

import java.util.Timer

import x7c1.wheat.modern.chrono.HasTimer
import x7c1.wheat.modern.kinds.FutureFate.HasContext

import scala.concurrent.ExecutionContext

object CustomContext {
  implicit object extract extends HasContext[CustomContext]{
    override def value = _.context
  }
  implicit object hasTimer extends HasTimer[CustomContext]{
    override val timer = new Timer()
  }
}

case class CustomContext(context: ExecutionContext)

case class CustomContextBoo(context: ExecutionContext)
