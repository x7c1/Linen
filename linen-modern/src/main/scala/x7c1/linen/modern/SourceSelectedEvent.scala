package x7c1.linen.modern


trait SourceSelectedEvent {
  def position: Int
  def source: Source
  def dump: String = s"sourceId:${source.id}, position:$position"
}

object SourceSelectedEvent {
  def apply(source: Source, position: Int): SourceSelectedEvent = {
    new SourceSelectedEventImpl(source, position)
  }
  private class SourceSelectedEventImpl(
    val source: Source,
    val position: Int) extends SourceSelectedEvent
}
