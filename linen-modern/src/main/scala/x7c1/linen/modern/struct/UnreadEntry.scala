package x7c1.linen.modern.struct


trait UnreadEntry {
  def sourceId: Long
  def entryId: Long
  def url: String
  def createdAt: Date
}

case class UnreadOutline(
  override val sourceId: Long,
  override val entryId: Long,
  override val url: String,
  override val createdAt: Date,
  shortTitle: String,
  shortContent: String ) extends UnreadEntry

case class UnreadDetail(
  override val sourceId: Long,
  override val entryId: Long,
  override val url: String,
  override val createdAt: Date,
  fullTitle: String,
  fullContent: String ) extends UnreadEntry
