package x7c1.linen.modern.accessor.database


sealed class AccountTagLabel(val text: String)

object Preset extends AccountTagLabel("preset")
