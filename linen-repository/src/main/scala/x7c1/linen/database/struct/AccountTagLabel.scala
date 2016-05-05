package x7c1.linen.database.struct

import x7c1.wheat.modern.database.selector.IdEndo

sealed class AccountTagLabel(val text: String)

object AccountTagLabel {
  implicit def id[A <: AccountTagLabel]: AccountTagLabelable[A] =
    new AccountTagLabelable[A] with IdEndo[A]
}

object PresetLabel extends AccountTagLabel("preset")

object ClientLabel extends AccountTagLabel("client")
