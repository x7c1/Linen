package x7c1.linen.modern

import x7c1.wheat.macros.logger.Log

trait Pane

class EntriesArea extends Pane {

  def displayOrLoad(sourceId: Long)(onFinish: EntriesLoadedEvent => Unit): Unit = {
    Log info s"$sourceId"

    onFinish(new EntriesLoadedEvent)
  }
}

class EntriesLoadedEvent

class SourcesArea extends Pane {

  def scrollTo(position: Int)(onFinish: SourceScrolledEvent => Unit): Unit = {
    Log info s"$position"

    onFinish(new SourceScrolledEvent)
  }
}

class SourceScrolledEvent