package x7c1.linen.modern.accessor.preset


sealed trait PresetRecordError

case class NoPresetAccount() extends PresetRecordError

case class NoPresetTag() extends PresetRecordError

case class UnexpectedException(cause: Exception) extends PresetRecordError
