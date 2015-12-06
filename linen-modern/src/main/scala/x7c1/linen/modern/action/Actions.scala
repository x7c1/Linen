package x7c1.linen.modern.action

import android.support.v7.widget.LinearLayoutManager
import x7c1.linen.modern.accessor.{EntryAccessor, SourceAccessor}
import x7c1.linen.modern.action.observer.{ItemSkippedEvent, SkippedEventFactory}
import x7c1.linen.modern.display.{EntryDetailSelectedEvent, EntrySelectedEvent, SourceSelectedEvent}
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
trait OnSourceSkipped{
  def onSourceSkipped(event: SourceSkippedEvent): CallbackTask[Unit]
}
trait OnEntryFocused {
  def onEntryFocused(event: EntryFocusedEvent): CallbackTask[Unit]
}
trait OnEntrySelected {
  def onEntrySelected(event: EntrySelectedEvent): CallbackTask[Unit]
}
trait OnEntryDetailFocused {
  def onEntryDetailFocused(event: EntryDetailFocusedEvent): CallbackTask[Unit]
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

case class SourceSkippedEvent(
  override val nextPosition: Int,
  nextSource: Source ) extends ItemSkippedEvent

class SourceSkippedEventFactory(
  layoutManager: LinearLayoutManager,
  sourceAccessor: SourceAccessor)

  extends SkippedEventFactory[SourceSkippedEvent]{

  override def create(): SourceSkippedEvent = {
    val next = 1 + layoutManager.findFirstCompletelyVisibleItemPosition()
    val source = sourceAccessor.get(next)
    SourceSkippedEvent(next, source)
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

case class EntryDetailFocusedEvent(
  override val position: Int,
  entry: Entry ) extends ItemFocusedEvent

class EntryDetailFocusedEventFactory(entryAccessor: EntryAccessor)
  extends FocusedEventFactory[EntryDetailFocusedEvent]{

  override def createAt(position: Int) = {
    val entry = entryAccessor get position
    EntryDetailFocusedEvent(position, entry)
  }
}
