package x7c1.wheat.modern.kinds

import scala.concurrent.ExecutionContext

object CustomContext {
  implicit object extract extends (CustomContext => ExecutionContext) {
    override def apply(v1: CustomContext) = v1.context
  }
}

case class CustomContext(context: ExecutionContext)

case class CustomContextBoo(context: ExecutionContext)
