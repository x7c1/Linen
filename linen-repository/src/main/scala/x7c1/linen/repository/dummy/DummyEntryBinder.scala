package x7c1.linen.repository.dummy

import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.database.struct.HasSourceId
import x7c1.linen.repository.loader.crawling.{CrawlerContext, LoadedEntry, SourceInspector, TraceableQueue, UpdatedSource}
import x7c1.wheat.macros.logger.Log

import scala.concurrent.{Await, Promise}

class DummyEntryBinder private (helper: DatabaseHelper){

  def bind[A: HasSourceId](sourceId: A, entries: Seq[LoadedEntry]): UpdatedSource = {

    val Right(inspectedSource) = SourceInspector(helper) inspectSource sourceId
    val queue = new TraceableQueue(
      helper = helper,
      sourceLoader = DummySourceLoader(entries)
    )
    val promise = Promise[UpdatedSource]
    queue.enqueueSource(inspectedSource).run(CrawlerContext){
      case Left(e) => Log error e.detail
      case Right(source) => promise.success(source)
    }
    import concurrent.duration._
    Await.result(promise.future, 3.seconds)
  }
}

object DummyEntryBinder {
  def apply(helper: DatabaseHelper): DummyEntryBinder = {
    new DummyEntryBinder(helper)
  }
}
