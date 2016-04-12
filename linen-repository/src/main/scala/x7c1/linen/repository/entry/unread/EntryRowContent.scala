package x7c1.linen.repository.entry.unread

sealed trait EntryRowContent[+A <: UnreadEntry]

case class SourceHeadlineContent(
  sourceId: Long,
  title: String ) extends EntryRowContent[Nothing]

case class EntryContent[+A <: UnreadEntry](
  entry: A) extends EntryRowContent[A]

case class FooterContent() extends EntryRowContent[Nothing]