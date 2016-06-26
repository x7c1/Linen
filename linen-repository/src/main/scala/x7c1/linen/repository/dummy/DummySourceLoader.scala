package x7c1.linen.repository.dummy

import java.util.concurrent.Executor

import x7c1.linen.repository.loader.crawling.{InspectedSource, LoadedEntry, LoadedSource, SourceLoader}
import x7c1.wheat.modern.kinds.Fate

import scala.concurrent.ExecutionContext

class DummySourceLoader private (entries: Seq[LoadedEntry]) extends SourceLoader {

  override def loadSource(source: InspectedSource) = Fate {
    new LoadedSource(
      sourceId = source.sourceId,
      title = source.title,
      description = source.description,
      entries = entries map Right.apply
    )
  }
}

object DummySourceLoader {
  def apply(f: Seq[LoadedEntry]): SourceLoader = {
    new DummySourceLoader(f)
  }
  object Implicits {
    /*
    Workaround to suppress following robolectric error:
      java.lang.IllegalStateException: Illegal connection pointer 1.
        Current pointers for thread Thread[pool-5-thread-1,5,main] []
    */
    implicit lazy val executor: ExecutionContext = {
      ExecutionContext fromExecutor new Executor {
        override def execute(command: Runnable) = command.run()
      }
    }
  }
}
