package x7c1.linen.database.struct

sealed class AccountTagLabel(val text: String)

object AccountTagLabel {
  implicit def id[A <: AccountTagLabel]: AccountTagLabelable[A] =
    new AccountTagLabelable[A] {
      override def idOf(target: A): AccountTagLabel = target
    }
}

object PresetLabel extends AccountTagLabel("preset")

object ClientLabel extends AccountTagLabel("client")
