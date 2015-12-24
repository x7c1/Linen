package x7c1.linen.modern.display

sealed trait PaneLabel

object PaneLabel {
  object SourceArea extends PaneLabel
  object EntryArea extends PaneLabel
  object EntryDetailArea extends PaneLabel
}
