package x7c1.linen.modern

import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.patch.TaskAsync

import scala.collection.mutable.ListBuffer

trait EntryAccessor {
  def get: Seq[Entry]
}

class EntryStorage extends EntryAccessor {

  override def get: Seq[Entry] = underlying

  def has(sourceId: Long): Boolean = {
    underlying.exists(_.sourceId == sourceId)
  }

  private lazy val underlying = ListBuffer[Entry]()

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
      title = s"entry-$n in source-$sourceId",
      content = s"sample $sourceId-$n " * 5,
      createdAt = LinenDate.dummy()
    )
  }
}

sealed trait EntriesLoadingResult

case class EntriesLoadSuccess(
  entries: Seq[Entry] ) extends EntriesLoadingResult