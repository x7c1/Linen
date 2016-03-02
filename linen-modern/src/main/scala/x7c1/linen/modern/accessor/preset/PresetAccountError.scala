package x7c1.linen.modern.accessor.preset


sealed trait PresetAccountError

case class NoPresetAccount() extends PresetAccountError

case class NoPresetTag() extends PresetAccountError

case class UnexpectedException(cause: Exception) extends PresetAccountError
