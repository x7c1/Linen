package x7c1.linen.modern.accessor

sealed trait UnreadRowKind

case object EntryKind extends UnreadRowKind

case object SourceKind extends UnreadRowKind
