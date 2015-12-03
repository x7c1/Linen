package x7c1.linen.modern.action

import x7c1.linen.modern.accessor.{EntryAccessor, SourceAccessor}
import x7c1.linen.modern.display.{SourceSelectedEvent, EntrySelectedEvent, EntryDetailSelectedEvent}
import x7c1.linen.modern.struct.{Entry, Source}
import x7c1.wheat.modern.callback.CallbackTask
import x7c1.wheat.modern.observer.{FocusedEventFactory, ItemFocusedEvent}

class Actions (
  val container: ContainerAction,
  val sourceArea: SourceAreaAction,
  val entryArea: EntryAreaAction,
  val detailArea: EntryDetailAreaAction,
  val prefetcher: PrefetcherAction
)

trait OnSourceSelected {
  def onSourceSelected(event: SourceSelectedEvent): CallbackTask[Unit]
}
trait OnSourceFocused {
  def onSourceFocused(event: SourceFocusedEvent): CallbackTask[Unit]
}
trait OnEntryFocused {
  def onEntryFocused(event: EntryFocusedEvent): CallbackTask[Unit]
}
trait OnEntrySelected {
  def onEntrySelected(event: EntrySelectedEvent): CallbackTask[Unit]
}
trait OnEntryDetailSelected {
  def onEntryDetailSelected(event: EntryDetailSelectedEvent): CallbackTask[Unit]
}

case class SourceFocusedEvent(
  override val position: Int,
  source: Source) extends ItemFocusedEvent

class SourceFocusedEventFactory(sourceAccessor: SourceAccessor)
  extends FocusedEventFactory[SourceFocusedEvent] {

  override def createAt(position: Int) = {
    val source = sourceAccessor get position
    SourceFocusedEvent(position, source)
  }
}

case class EntryFocusedEvent(
  override val position: Int,
  entry: Entry) extends ItemFocusedEvent

class EntryFocusedEventFactory(entryAccessor: EntryAccessor)
  extends FocusedEventFactory[EntryFocusedEvent] {

  override def createAt(position: Int) = {
    val entry = entryAccessor get position
    EntryFocusedEvent(position, entry)
  }
}
