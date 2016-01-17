package x7c1.linen.modern.struct

case class UnreadSource(
  id: Long,
  url: String,
  title: String,
  description: String,
  startEntryId: Option[Long]
)
