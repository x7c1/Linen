package x7c1.linen.modern

import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.patch.TaskAsync

import scala.collection.mutable.ListBuffer

trait EntryAccessor {
  def get: Seq[Entry]
}

class EntryBuffer extends EntryAccessor {

  private val underlying = ListBuffer[Entry]()

  override def get: Seq[Entry] = underlying

  def insertAfter(entryId: Long, entries: Seq[Entry]) = {
    val position = underlying.indexWhere(_.entryId == entryId) + 1
    underlying.insertAll(position, entries)
  }
  def indexOf(entryId: Long): Int =
    underlying.indexWhere(_.entryId == entryId)

  def appendAll(entries: Seq[Entry]): Unit = {
    underlying ++= entries
  }
}

object EntryLoader {
  def load(sourceId: Long)(onFinish: EntriesLoadingResult => Unit) = {
    // dummy
    TaskAsync.run(delay = 1000){
      Log info s"[done] source-$sourceId"
      onFinish(new EntriesLoadSuccess(createDummy(sourceId)))
    }
  }
  def createDummy(sourceId: Long) = (1 to 5) map { n =>
    Entry(
      sourceId = sourceId,
      entryId = sourceId * 1000 + n,
      url = s"http://example.com/source-$sourceId/entry-$n",
      title = s"$sourceId-$n entry",
      content = s"sample content $sourceId-$n " * 5,
      createdAt = LinenDate.dummy()
    )
  }
}

sealed trait EntriesLoadingResult

case class EntriesLoadSuccess(
  entries: Seq[Entry] ) extends EntriesLoadingResult
