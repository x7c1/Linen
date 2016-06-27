package x7c1.wheat.modern.callback.either

import x7c1.wheat.macros.reify.HasConstructor


trait SampleError {
  def message: String
}
object SampleError {
  implicit object constructor extends HasConstructor[Throwable => SampleError]{
    override def newInstance = e => new SampleError {
      override def message = e.getMessage
    }
  }
}

class SampleSubError(x: String) extends SampleError {
  def decorated = s"[$message]"

  override def message: String = x
}
