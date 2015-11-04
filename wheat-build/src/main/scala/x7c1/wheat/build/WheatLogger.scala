package x7c1.wheat.build

import sbt.Logger

class WheatLogger(logger: Logger){
  def info(message: String): Unit = {
    logger info s"${Console.BLUE}[wheat]${Console.RESET} $message"
  }
}

object WheatLogger {
  def apply(logger: Logger): WheatLogger = new WheatLogger(logger)
}
