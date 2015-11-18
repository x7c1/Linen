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

  def indexOf(entryId: Long): Int =
    underlying.indexWhere(_.entryId == entryId)

  def positionAfter(entryId: Option[Long]) = {
    entryId match {
      case Some(id) => underlying.indexWhere(_.entryId == id) + 1
      case _ => 0
    }
  }
  def insertAll(position: Int, entries: Seq[Entry]) = {
    underlying.insertAll(position, entries)
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
