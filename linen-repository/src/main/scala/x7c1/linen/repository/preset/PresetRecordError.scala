package x7c1.linen.repository.preset

import x7c1.linen.repository.error.ApplicationError

sealed trait PresetRecordError

case class NoPresetAccount() extends PresetRecordError

case class UnexpectedException(cause: Exception) extends PresetRecordError

case class UnexpectedError(cause: ApplicationError) extends PresetRecordError
