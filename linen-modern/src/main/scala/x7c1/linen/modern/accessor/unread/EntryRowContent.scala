package x7c1.linen.modern.accessor.unread

import x7c1.linen.modern.struct.UnreadEntry

sealed trait EntryRowContent[+A <: UnreadEntry]

case class SourceHeadlineContent(
  sourceId: Long,
  title: String ) extends EntryRowContent[Nothing]

case class EntryContent[+A <: UnreadEntry](
  entry: A) extends EntryRowContent[A]
