package x7c1.linen.repository.loader.crawling

import java.util.concurrent.atomic.AtomicInteger

import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.database.struct.{HasAccountId, HasChannelId}
import x7c1.linen.repository.loader.crawling.QueueingEvent.{OnDone, OnProgress}
import x7c1.linen.repository.source.setting.{SettingSource, SettingSourceAccessorFactory}
import x7c1.wheat.macros.logger.Log

import scala.concurrent.ExecutionContext

class ChannelLoaderQueueing private (helper: DatabaseHelper, queue: TraceableQueue){

  def start[A: HasAccountId, B: HasChannelId]
    (account: A, channel: B)
    (callback: QueueingEvent => Unit)(implicit x: ExecutionContext) = {

    val inspectedSources = {
      val inspector = SourceInspector(helper)
      val accessor = SettingSourceAccessorFactory(helper, account).create(channel)
      val settingSources = 0 until accessor.length flatMap accessor.findAt
      settingSources map inspector.inspectSource[SettingSource]
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
    (callback: QueueingEvent => Unit)(implicit x: ExecutionContext) = {

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
      queue.enqueueSource(source) onComplete { _ => onProgress() }
    }
  }

}

object ChannelLoaderQueueing {
  def apply(helper: DatabaseHelper, queue: TraceableQueue): ChannelLoaderQueueing = {
    new ChannelLoaderQueueing(helper, queue)
  }
}
