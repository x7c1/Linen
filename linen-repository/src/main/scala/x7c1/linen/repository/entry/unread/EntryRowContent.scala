package x7c1.linen.repository.entry.unread

import x7c1.linen.repository.entry.unread.selector.EntryRowSelector
import x7c1.wheat.modern.database.selector.SelectorProvidable

sealed trait EntryRowContent[+A <: UnreadEntry] {
  def sourceId: Option[Long]
}

object EntryRowContent {
  implicit def providable[A <: UnreadEntry]: SelectorProvidable[EntryRowContent[A], EntryRowSelector[A]] = {
    new SelectorProvidable[EntryRowContent[A], EntryRowSelector[A]]
  }
}

case class SourceHeadlineContent(
  rawSourceId: Long,
  title: String ) extends EntryRowContent[Nothing] {

  override def sourceId: Option[Long] = Some(rawSourceId)
}

case class EntryContent[+A <: UnreadEntry](entry: A) extends EntryRowContent[A] {
  override def sourceId: Option[Long] = Some(entry.sourceId)
}

case class FooterContent() extends EntryRowContent[Nothing] {
  override def sourceId: Option[Long] = None
}
