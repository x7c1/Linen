package x7c1.linen.modern.action.observer

import java.util.concurrent.Executors

import x7c1.wheat.modern.callback.CallbackTask

import scala.concurrent.{ExecutionContext, Future}


object CallbackTaskRunner {
  implicit lazy val executor = ExecutionContext fromExecutor {
    Executors.newSingleThreadExecutor()
  }
  def runAsync[A](onError: Throwable => Unit)(task: CallbackTask[A]): Unit = {
    Future { task.execute() } onFailure {
      case e: Exception => onError(e)
    }
  }
}
