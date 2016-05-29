package x7c1.linen.repository.loader.crawling

sealed trait QueueingEvent

object QueueingEvent {
  case class OnProgress(
    current: Int,
    max: Int ) extends QueueingEvent

  case class OnDone(max: Int) extends QueueingEvent
}

