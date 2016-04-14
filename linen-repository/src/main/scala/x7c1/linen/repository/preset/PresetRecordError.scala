package x7c1.linen.repository.preset

import x7c1.linen.repository.error.ApplicationError
import x7c1.wheat.modern.formatter.ThrowableFormatter

sealed trait PresetRecordError extends ApplicationError

case class NoPresetAccount() extends PresetRecordError {
  override def detail: String = "preset account not found"
}

case class UnexpectedException(cause: Exception) extends PresetRecordError {
  override def detail: String = ThrowableFormatter.format(cause){"[error]"}
}

case class UnexpectedError(cause: ApplicationError) extends PresetRecordError {
  override def detail: String = cause.detail
}
