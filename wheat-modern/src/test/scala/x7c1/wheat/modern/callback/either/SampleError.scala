package x7c1.wheat.modern.callback.either


trait SampleError {
  def message: String
}
object SampleError {
  implicit class Unexpected(e: Throwable) extends SampleError {
    override def message = e.getMessage
  }
}

class SampleSubError(x: String) extends SampleError {
  def decorated = s"[$message]"

  override def message: String = x
}
