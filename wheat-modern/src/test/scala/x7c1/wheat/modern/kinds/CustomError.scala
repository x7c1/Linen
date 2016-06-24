package x7c1.wheat.modern.kinds

import x7c1.wheat.macros.reify.HasConstructor


sealed trait CustomError {
  def message: String
}

object CustomError {
  implicit object reify extends HasConstructor[Throwable => CustomError]{
    override def newInstance = e => UnexpectedError(e)
  }
}

case class UnexpectedError(cause: Throwable) extends CustomError {
  override def message = cause.getMessage
}
