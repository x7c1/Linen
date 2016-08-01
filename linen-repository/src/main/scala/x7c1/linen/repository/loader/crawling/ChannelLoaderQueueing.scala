package x7c1.linen.repository.loader.crawling

import java.util.concurrent.atomic.AtomicInteger

import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.database.struct.HasChannelStatusKey
import x7c1.linen.repository.loader.crawling.QueueingEvent.{OnDone, OnProgress}
import x7c1.linen.repository.source.setting.SettingSource
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.formatter.ThrowableFormatter.format

class ChannelLoaderQueueing private (helper: DatabaseHelper, queue: TraceableQueue){

  def start[A: HasChannelStatusKey](key: A)(callback: QueueingEvent => Unit): Unit = {
    val inspectedSources = {
      val inspector = SourceInspector(helper)
      helper.selectorOf[SettingSource] traverseOn key match {
        case Left(e) =>
          Log error format(e){"[failed]"}
          Seq()
        case Right(accessor) =>
          val settingSources = 0 until accessor.length flatMap accessor.findAt
          settingSources map inspector.inspectSource[SettingSource]
      }
    }
    val targetSources = inspectedSources collect {
      case Right(sources) => sources
    }
    inspectedSources collect {
      case Left(error) => Log error error.message
    }
    enqueueSources(targetSources)(callback)
  }

  private def enqueueSources
    (sources: Seq[InspectedSource])
    (callback: QueueingEvent => Unit) = {

    val progress = new AtomicInteger(0)
    val max = sources.length

    def onProgress() = {
      val current = progress.incrementAndGet()
      callback apply OnProgress(
        current = current,
        max = max
      )
      if (current == max){
        callback apply OnDone(max)
      }
    }
    if (max == 0){
      callback apply OnDone(max)
    } else sources foreach { source =>
      queue.enqueueSource(source).run(CrawlerContext){
        case Left(e) =>
          Log error s"$source\n${e.detail}"
          onProgress()
        case Right(updated) =>
          onProgress()
      }
    }
  }

}

object ChannelLoaderQueueing {
  def apply(helper: DatabaseHelper, queue: TraceableQueue): ChannelLoaderQueueing = {
    new ChannelLoaderQueueing(helper, queue)
  }
}
