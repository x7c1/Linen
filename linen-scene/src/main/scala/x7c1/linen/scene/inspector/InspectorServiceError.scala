package x7c1.linen.scene.inspector


import x7c1.wheat.modern.fate.FateProvider.ErrorLike

sealed trait InspectorServiceError {
  def message: String
}

object InspectorServiceError {

  implicit object error extends ErrorLike[InspectorServiceError] {
    override def newInstance = new UnexpectedError(_)
  }

  class UnexpectedError(e: Throwable) extends InspectorServiceError {
    override def message: String = e.getMessage
  }

}
