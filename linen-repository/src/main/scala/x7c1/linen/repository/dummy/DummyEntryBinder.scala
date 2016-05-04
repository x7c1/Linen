package x7c1.linen.repository.dummy

import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.database.struct.SourceIdentifiable
import x7c1.linen.repository.crawler.{LoadedEntry, SourceInspector, UpdatedSource}
import x7c1.wheat.modern.formatter.ThrowableFormatter

import scala.concurrent.{Await, ExecutionContext}

class DummyEntryBinder private (helper: DatabaseHelper){

  def bind[A: SourceIdentifiable](sourceId: A, entries: Seq[LoadedEntry])
      (implicit x: ExecutionContext): UpdatedSource = {

    val Right(inspectedSource) = SourceInspector(helper) inspectSource sourceId
    val queue = new TraceableQueue(
      helper = helper,
      sourceLoader = DummySourceLoader(entries)
    )
    val future = queue enqueueSource inspectedSource
    future onFailure {
      case e =>
        val message = ThrowableFormatter.format(e){"[failed]"}
        println(message)
    }
    import concurrent.duration._
    Await.result(future, 3.seconds)
  }
}

object DummyEntryBinder {
  def apply(helper: DatabaseHelper): DummyEntryBinder = {
    new DummyEntryBinder(helper)
  }
}
