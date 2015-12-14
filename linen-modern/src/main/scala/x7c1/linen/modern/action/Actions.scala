package x7c1.linen.modern.action

import android.support.v7.widget.LinearLayoutManager
import x7c1.linen.modern.accessor.{EntryAccessor, SourceAccessor}
import x7c1.linen.modern.action.observer.{ItemSkippedEvent, SkippedEventFactory}
import x7c1.linen.modern.display.{EntryDetailSelectedEvent, EntrySelectedEvent, SourceSelectedEvent}
import x7c1.linen.modern.struct.{EntryDetail, EntryOutline, Source}
import x7c1.wheat.modern.callback.CallbackTask
import x7c1.wheat.modern.observer.{FocusedEventFactory, ItemFocusedEvent}

class Actions (
  val container: ContainerAction,
  val sourceArea: SourceAreaAction,
  val entryArea: EntryAreaAction,
  val detailArea: EntryDetailAreaAction
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
    sourceAccessor findAt position map { source =>
      SourceFocusedEvent(position, source)
    }
  }
}

case class SourceSkippedEvent(
  override val nextPosition: Int,
  nextSource: Source ) extends ItemSkippedEvent

class SourceSkippedEventFactory(
  layoutManager: LinearLayoutManager,
  sourceAccessor: SourceAccessor)

  extends SkippedEventFactory[SourceSkippedEvent]{

  override def create() = {
    val next = 1 + layoutManager.findFirstCompletelyVisibleItemPosition()
    sourceAccessor findAt next map { source =>
      SourceSkippedEvent(next, source)
    }
  }
}

case class EntryFocusedEvent(
  override val position: Int,
  entry: EntryOutline) extends ItemFocusedEvent

class EntryFocusedEventFactory(entryAccessor: EntryAccessor[EntryOutline])
  extends FocusedEventFactory[EntryFocusedEvent] {

  override def createAt(position: Int) = {
    entryAccessor findAt position map { entry =>
      EntryFocusedEvent(position, entry)
    }
  }
}

case class EntryDetailFocusedEvent(
  override val position: Int,
  entry: EntryDetail ) extends ItemFocusedEvent

class EntryDetailFocusedEventFactory(entryAccessor: EntryAccessor[EntryDetail])
  extends FocusedEventFactory[EntryDetailFocusedEvent]{

  override def createAt(position: Int) = {
    entryAccessor findAt position map { entry =>
      EntryDetailFocusedEvent(position, entry)
    }
  }
}
