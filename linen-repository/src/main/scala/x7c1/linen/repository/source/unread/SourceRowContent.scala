package x7c1.linen.repository.source.unread

import x7c1.linen.database.struct.SourceIdentifiable

sealed trait SourceRowContent

case class UnreadSource(
  id: Long,
  url: String,
  title: String,
  description: String,
  rating: Int,
  accountId: Long,
  latestEntryId: Long,
  latestEntryCreatedAt: Int,
  startEntryId: Option[Long],
  startEntryCreatedAt: Option[Int] ) extends SourceRowContent

case class SourceFooterContent() extends SourceRowContent

object UnreadSource {
  implicit object id extends SourceIdentifiable[UnreadSource] {
    override def sourceId(target: UnreadSource): Long = target.id
  }
}
