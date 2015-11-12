package x7c1.linen.modern


trait SourceSelectedEvent {
  def sourceId: Long
  def position: Int
}

object SourceSelectedEvent {
  import scala.language.implicitConversions

  def apply(source: Source, position: Int): SourceSelectedEvent = {
    new SourceSelectedEventImpl(source.id, position)
  }

  implicit def dumpToString(e: SourceSelectedEvent): String = {
    s"sourceId:${e.sourceId}, position:${e.position}"
  }

  private case class SourceSelectedEventImpl(
    sourceId: Long,
    position: Int) extends SourceSelectedEvent
}
