package x7c1.linen.modern.accessor

import x7c1.linen.modern.struct.{Date, Entry}

trait EntryAccessor {

  def get(position: Int): Entry

  def length: Int

  def firstEntryIdOf(sourceId: Long): Option[Long]

  def indexOf(entryId: Long): Int
}

class EntryBuffer extends EntryAccessor {

  override def get(position: Int): Entry = {
    val sourceId = (position / 10) + 1
    val n = position % 10 + 1
    Entry(
      sourceId = sourceId,
      entryId = sourceId * 1000 + n,
      url = s"http://example.com/source-$sourceId/entry-$n",
      title = s"$sourceId-$n entry " * 3,
      content = s"$sourceId-$n foo bar " * 200,
      createdAt = Date.dummy()
    )
  }
  override def length = 300 * 10

  override def indexOf(entryId: Long): Int = {
    val mod = entryId % 1000 - 1
    val n = entryId / 1000 - 1
    (n * 10 + mod).toInt
  }
  override def firstEntryIdOf(sourceId: Long): Option[Long] = {
    Some(sourceId * 1000 + 1)
  }

}
