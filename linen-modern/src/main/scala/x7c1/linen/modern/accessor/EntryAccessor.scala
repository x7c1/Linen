package x7c1.linen.modern.accessor

import x7c1.linen.modern.struct.Entry

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import x7c1.wheat.modern.callback.OnFinish

trait EntryAccessor {

  def get(position: Int): Entry

  def length: Int

  def firstEntryIdOf(sourceId: Long): Option[Long]

  def indexOf(entryId: Long): Int
}

class EntryBuffer(entryInsertedListener: OnEntryInsertedListener) extends EntryAccessor {

  private val underlying = ListBuffer[Entry]()

  private val entriesMapping = mutable.Map[Long, Seq[Long]]()

  override def get(position: Int): Entry = underlying(position)

  override def length = underlying.length

  override def indexOf(entryId: Long): Int =
    underlying.indexWhere(_.entryId == entryId)

  def positionAfter(entryId: Option[Long]) = {
    entryId match {
      case Some(id) => underlying.indexWhere(_.entryId == id) + 1
      case _ => 0
    }
  }
  def insertAll(position: Int, sourceId: Long, entries: Seq[Entry])(done: OnFinish): Unit = {
    val newer = entries filterNot { this has _.sourceId }
    underlying.insertAll(position, newer)
    entriesMapping(sourceId) = entries.map(_.entryId)

    val event = EntryInsertedEvent(position, newer.length)
    entryInsertedListener.onInserted(event)(done)
  }

  def has(sourceId: Long): Boolean = {
    entriesMapping.get(sourceId).exists(_.nonEmpty)
  }
  override def firstEntryIdOf(sourceId: Long): Option[Long] = {
    entriesMapping.get(sourceId).flatMap(_.headOption)
  }
  def lastEntryIdOf(sourceId: Long): Option[Long] = {
    entriesMapping.get(sourceId).flatMap(_.lastOption)
  }

}
