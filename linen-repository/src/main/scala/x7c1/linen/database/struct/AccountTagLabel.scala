package x7c1.linen.database.struct

sealed class AccountTagLabel(val text: String)

object PresetLabel extends AccountTagLabel("preset")

object ClientLabel extends AccountTagLabel("client")
