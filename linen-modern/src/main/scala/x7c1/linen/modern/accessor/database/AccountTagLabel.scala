package x7c1.linen.modern.accessor.database


sealed class AccountTagLabel(val text: String)

object PresetLabel extends AccountTagLabel("preset")

object ClientLabel extends AccountTagLabel("client")
