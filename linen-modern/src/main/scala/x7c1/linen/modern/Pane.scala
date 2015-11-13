package x7c1.linen.modern

import x7c1.wheat.macros.logger.Log

trait Pane {
  def displayPosition: Int
}

class EntriesArea(
  override val displayPosition: Int) extends Pane {

  def displayOrLoad(sourceId: Long)(onFinish: EntriesLoadedEvent => Unit): Unit = {
    Log info s"[init] sourceId:$sourceId"

    Thread sleep 500
    onFinish(new EntriesLoadedEvent)
  }
}

class EntriesLoadedEvent

class SourcesArea(
  override val displayPosition: Int) extends Pane {

  def scrollTo(position: Int)(onFinish: SourceScrolledEvent => Unit): Unit = {
    Log info s"[init] position:$position"

    Thread sleep 500
    onFinish(new SourceScrolledEvent)
  }
}

class SourceScrolledEvent
