package x7c1.wheat.modern.kinds

import java.util.Timer

import x7c1.wheat.modern.features.HasSharedInstance
import x7c1.wheat.modern.kinds.FutureFate.HasContext

import scala.concurrent.ExecutionContext

object CustomContext {
  implicit object extract extends HasContext[CustomContext]{
    override def value = _.context
  }
  implicit object timer extends HasSharedInstance[CustomContext, Timer]{
    override val instance = new Timer()
  }
}

case class CustomContext(context: ExecutionContext)

case class CustomContextBoo(context: ExecutionContext)
