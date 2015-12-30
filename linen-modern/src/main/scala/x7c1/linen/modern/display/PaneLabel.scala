package x7c1.linen.modern.display

sealed trait PaneLabel

object PaneLabel {
  case object SourceArea extends PaneLabel
  case object EntryArea extends PaneLabel
  case object EntryDetailArea extends PaneLabel
}
