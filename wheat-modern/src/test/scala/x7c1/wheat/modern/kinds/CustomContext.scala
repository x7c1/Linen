package x7c1.wheat.modern.kinds

import java.util

import x7c1.wheat.modern.fate.FutureFate.{HasContext, HasTimer}

import scala.concurrent.ExecutionContext

object CustomContext extends CustomContext {
  implicit object hasContext extends HasContext[CustomContext]{
    override def instance = _.context
  }
  implicit object hasTimer extends HasTimer[CustomContext]{
    override def instance = _.timer
  }
}

trait CustomContext {
  private val context = ExecutionContext.global
  private val timer = new util.Timer()
}

case class CustomContextBoo(context: ExecutionContext)
