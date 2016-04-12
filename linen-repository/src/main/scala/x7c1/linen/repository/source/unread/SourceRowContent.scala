package x7c1.linen.repository.source.unread

sealed trait SourceRowContent

case class UnreadSource(
  id: Long,
  url: String,
  title: String,
  description: String,
  rating: Int,
  latestEntryId: Long,
  startEntryId: Option[Long] ) extends SourceRowContent

case class SourceFooterContent() extends SourceRowContent
