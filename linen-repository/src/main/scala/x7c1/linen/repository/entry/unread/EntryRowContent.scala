package x7c1.linen.repository.entry.unread

sealed trait EntryRowContent[+A <: UnreadEntry] {
  def sourceId: Option[Long]
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
