package x7c1.linen.modern

import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.patch.TaskAsync

import scala.collection.mutable
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

class EntryLoader {

  private val cache = new mutable.HashMap[Long, Seq[Entry]]

  def load(sourceId: Long)(onFinish: EntriesLoadingResult => Unit) =
    cache.get(sourceId) match {
      case Some(entries) => onFinish(new EntriesLoadSuccess(entries))
      case None =>
        TaskAsync.run(delay = 500){
          Log info s"[done] source-$sourceId"
          onFinish(new EntriesLoadSuccess(createDummy(sourceId)))
        }
    }

  private val prefetching = mutable.Map[Long, Boolean]()

  def prefetch(sourceId: Long)(onFinish: EntriesPrefetchTriggered => Unit): Unit = {
    if (cache.get(sourceId).nonEmpty){
      Log info s"[cancel] source-$sourceId already cached"
      return
    }
    if (prefetching.getOrElse(sourceId, false)){
      Log info s"[cancel] source-$sourceId already triggered"
      return
    }
    prefetching(sourceId) = true

    // dummy
    TaskAsync.run(delay = 500){
      cache(sourceId) = createDummy(sourceId)
      prefetching(sourceId) = false

      Log debug s"[done] source-$sourceId"
      onFinish(new EntriesPrefetchTriggered(sourceId))
    }
  }

  def createDummy(sourceId: Long) = (1 to 50) map { n =>
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

case class EntriesPrefetchTriggered(
  sourceId: Long
)