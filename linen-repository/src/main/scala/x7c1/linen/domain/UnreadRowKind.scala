package x7c1.linen.domain

sealed trait UnreadRowKind

case object EntryKind extends UnreadRowKind

case object SourceKind extends UnreadRowKind

case object FooterKind extends UnreadRowKind
