package x7c1.linen.modern.struct


trait Entry {
  def sourceId: Long
  def entryId: Long
  def url: String
  def createdAt: Date
}

case class EntryOutline(
  override val sourceId: Long,
  override val entryId: Long,
  override val url: String,
  override val createdAt: Date,
  shortTitle: String,
  shortContent: String ) extends Entry

case class EntryDetail(
  override val sourceId: Long,
  override val entryId: Long,
  override val url: String,
  override val createdAt: Date,
  fullTitle: String,
  fullContent: String ) extends Entry
