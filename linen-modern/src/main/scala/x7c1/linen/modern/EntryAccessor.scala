package x7c1.linen.modern

import android.support.v7.widget.{LinearLayoutManager, RecyclerView}
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.callback.OnFinish
import x7c1.wheat.modern.patch.TaskAsync

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.util.Random

trait EntryAccessor {

  def get(position: Int): Entry

  def length: Int

  def firstEntryIdOf(sourceId: Long): Option[Long]

  def indexOf(entryId: Long): Int
}

class EntryBuffer(entryInsertedListener: OnEntryInsertedListener) extends EntryAccessor {

  private val underlying = ListBuffer[Entry]()

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
    entryInsertedListener.onInserted(position, newer.length)(done)
  }

  private lazy val entriesMapping = mutable.Map[Long, Seq[Long]]()

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

trait OnEntryInsertedListener { self =>

  import x7c1.wheat.modern.callback.CallbackTask.task
  import x7c1.wheat.modern.callback.Imports._

  def onInserted(position: Int, length: Int)(done: OnFinish): Unit

  def append(listener: OnEntryInsertedListener): OnEntryInsertedListener =
    new OnEntryInsertedListener {
      override def onInserted(position: Int, length: Int)(done: OnFinish): Unit = {
        val f = for {
          _ <- task of self.onInserted(position, length) _
          _ <- task of listener.onInserted(position, length) _
        } yield {
          done.evaluate()
        }
        f.execute()
      }
    }
}

class EntryCacher {

  private val cache = new mutable.HashMap[Long, Seq[Entry]]

  def findCache(sourceId: Long): Option[Seq[Entry]] = {
    cache.get(sourceId)
  }
  def updateCache(sourceId: Long, entries: Seq[Entry]) = {
    cache(sourceId) = entries
  }
}

class EntryLoader (cacher: EntryCacher, listener: OnEntryLoadedListener){

  def load(sourceId: Long) =
    cacher.findCache(sourceId) match {
      case Some(entries) =>
        listener.onEntryLoaded(new EntryLoadedEvent(sourceId, entries))
      case None =>
        TaskAsync.run(delay = 500){
          Log info s"[done] source-$sourceId"
          val entries = createDummy(sourceId)
          cacher.updateCache(sourceId, entries)
          listener.onEntryLoaded(new EntryLoadedEvent(sourceId, entries))
        }
    }

  def createDummy(sourceId: Long) = (1 to 10) map { n =>
    Entry(
      sourceId = sourceId,
      entryId = sourceId * 1000 + n,
      url = s"http://example.com/source-$sourceId/entry-$n",
      title = s"$sourceId-$n entry " + DummyString.words(10),
      content = s"$sourceId-$n " + DummyString.words(100),
      createdAt = LinenDate.dummy()
    )
  }
}

object DummyString {
  def word: String = {
    val wordRange = 3 to 10
    val wordLength = wordRange(Random.nextInt(wordRange.size - 1))
    Random.alphanumeric.take(wordLength).mkString
  }
  def words(n: Int): String = {
    (0 to n).map(_ => word).mkString(" ")
  }
}

object EntryLoader {
  def apply(cacher: EntryCacher): EntryLoadExecutor = new EntryLoadExecutor(cacher)
}

class EntryLoadExecutor(cacher: EntryCacher){
  def load(sourceId: Long)(f: EntryLoadedEvent => Unit) = {
    val listener = new OnEntryLoadedListener {
      override def onEntryLoaded(e: EntryLoadedEvent): Unit = f(e)
    }
    new EntryLoader(cacher, listener) load sourceId
  }
}

class SourceStateUpdater(
  sourceStateBuffer: SourceStateBuffer) extends OnEntryLoadedListener {

  override def onEntryLoaded(event: EntryLoadedEvent): Unit = {
    sourceStateBuffer.updateState(event.sourceId, SourcePrefetched)
  }
}

class SourceChangedNotifier(
  sourceAccessor: SourceAccessor,
  recyclerView: RecyclerView) extends OnEntryLoadedListener {

  import x7c1.wheat.modern.decorator.Imports._

  override def onEntryLoaded(event: EntryLoadedEvent): Unit = {
    recyclerView runUi { view =>
      sourceAccessor.positionOf(event.sourceId).
        foreach(view.getAdapter.notifyItemChanged)
    }
  }
}

class InsertedEntriesNotifier (
  recyclerView: RecyclerView) extends OnEntryInsertedListener {

  import x7c1.wheat.modern.decorator.Imports._

  private lazy val layoutManager = {
    recyclerView.getLayoutManager.asInstanceOf[LinearLayoutManager]
  }
  override def onInserted(position: Int, length: Int)(done: OnFinish): Unit = {
    Log debug s"[init] position:$position, length:$length"

    recyclerView runUi { view =>
      val current = layoutManager.findFirstCompletelyVisibleItemPosition()
      val base = if(current == position) -1 else 0
      view.getAdapter.notifyItemRangeInserted(position + base, length)
      done.evaluate()
    }
  }
}
