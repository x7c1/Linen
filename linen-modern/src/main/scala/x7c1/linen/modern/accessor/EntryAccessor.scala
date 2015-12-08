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
    DummyCreator.createEntryAt(position)
  }
  override def length = {
    DummyCreator.entryLength
  }
  override def indexOf(entryId: Long): Int = {
    DummyCreator.entryIndexOf(entryId)
  }
  override def firstEntryIdOf(sourceId: Long): Option[Long] = {
    DummyCreator.firstEntryIdOf(sourceId)
  }

}

object DummyCreator {

  def sourceLength = 300

  def entryLength = sourceLength * entriesPerSource

  def entriesPerSource = 10

  def createEntriesOf(sourceId: Long): Seq[Entry] =
    (1 to entriesPerSource) map { n =>
      createEntry(sourceId, n)
    }

  def createEntryAt(position: Int): Entry = {
    val sourceId = (position / entriesPerSource) + 1
    val n = position % entriesPerSource + 1
    createEntry(sourceId, n)
  }

  def createEntry(sourceId: Long, n: Int) = {
    Entry(
      sourceId = sourceId,
      entryId = sourceId * 1000 + n,
      url = s"http://example.com/source-$sourceId/entry-$n",
      title = s"$sourceId-$n entry " * 3,
      content = s"$sourceId-$n foo bar " * 200,
      createdAt = Date.dummy()
    )
  }

  def entryIndexOf(entryId: Long): Int = {
    val mod = entryId % 1000 - 1
    val n = entryId / 1000 - 1
    (n * entriesPerSource + mod).toInt
  }

  def firstEntryIdOf(sourceId: Long): Option[Long] = {
    Some(sourceId * 1000 + 1)
  }
}
