package x7c1.linen.repository.source.unread

import x7c1.linen.database.struct.HasSourceId
import x7c1.linen.repository.source.unread.selector.SourceRowSelector
import x7c1.wheat.modern.database.selector.SelectorProvidable

sealed trait SourceRowContent

object SourceRowContent {
  implicit object providable extends SelectorProvidable[SourceRowContent, SourceRowSelector]
}

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
  implicit object id extends HasSourceId[UnreadSource] {
    override def toId = _.id
  }
}
