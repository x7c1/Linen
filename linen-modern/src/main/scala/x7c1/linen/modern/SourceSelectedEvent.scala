package x7c1.linen.modern


trait SourceSelectedEvent {
  def sourceId: Long
  def position: Int
  def dump: String = s"sourceId:$sourceId, position:$position"
}

object SourceSelectedEvent {
  def apply(source: Source, position: Int): SourceSelectedEvent = {
    new SourceSelectedEventImpl(source.id, position)
  }
  private class SourceSelectedEventImpl(
    val sourceId: Long,
    val position: Int) extends SourceSelectedEvent
}
