package x7c1.linen.modern.action

import x7c1.linen.modern.accessor.{EntryAccessor, SourceAccessor}
import x7c1.linen.modern.display.{PaneFlungEvent, EntryDetailSelectedEvent, EntrySelectedEvent, SourceSelectedEvent}
import x7c1.linen.modern.struct.{EntryDetail, EntryOutline, Source}
import x7c1.wheat.modern.callback.CallbackTask
import x7c1.wheat.modern.observer.{FocusedEventFactory, ItemFocusedEvent, ItemSkippedEvent, ItemSkippedEventFactory, SkipStoppedEvent, SkipStoppedEventFactory}

class Actions (
  val container: ContainerAction,
  val sourceArea: SourceAreaAction,
  val entryArea: EntryAreaAction,
  val detailArea: EntryDetailAreaAction
)
trait OnPaneFlung {
  def onPaneFlung(event: PaneFlungEvent): Boolean
}
trait OnSourceSelected {
  def onSourceSelected(event: SourceSelectedEvent): CallbackTask[Unit]
}
trait OnSourceFocused {
  def onSourceFocused(event: SourceFocusedEvent): CallbackTask[Unit]
}
trait OnSourceSkipped{
  def onSourceSkipped(event: SourceSkippedEvent): CallbackTask[Unit]
}
trait OnSourceSkipStopped {
  def onSourceSkipStopped(event: SourceSkipStopped): CallbackTask[Unit]
}
trait OnEntryFocused {
  def onEntryFocused(event: EntryFocusedEvent): CallbackTask[Unit]
}
trait OnEntrySelected {
  def onEntrySelected(event: EntrySelectedEvent): CallbackTask[Unit]
}
trait OnEntrySkipped {
  def onEntrySkipped(event: EntrySkippedEvent): CallbackTask[Unit]
}
trait OnEntrySkipStopped {
  def onEntrySkipStopped(event: EntrySkipStopped): CallbackTask[Unit]
}
trait OnEntryDetailFocused {
  def onEntryDetailFocused(event: EntryDetailFocusedEvent): CallbackTask[Unit]
}
trait OnEntryDetailSelected {
  def onEntryDetailSelected(event: EntryDetailSelectedEvent): CallbackTask[Unit]
}
trait OnEntryDetailSkipped {
  def onEntryDetailSkipped(event: EntrySkippedEvent): CallbackTask[Unit]
}
trait OnEntryDetailSkipStopped {
  def onEntryDetailSkipStopped(event: EntrySkipStopped): CallbackTask[Unit]
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

class SourceSkippedEventFactory(sourceAccessor: SourceAccessor)
  extends ItemSkippedEventFactory[SourceSkippedEvent]{

  override def createAt(next: Int) = {
    sourceAccessor findAt next map { source =>
      SourceSkippedEvent(next, source)
    }
  }
}

case class SourceSkipStopped(
  override val currentPosition: Int,
  currentSource: Source ) extends SkipStoppedEvent

class SourceSkipStoppedFactory(sourceAccessor: SourceAccessor)
  extends SkipStoppedEventFactory[SourceSkipStopped]{

  override def createAt(current: Int) = {
    sourceAccessor findAt current map { source =>
      SourceSkipStopped(current, source)
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

case class EntrySkippedEvent(
  override val nextPosition: Int,
  nextEntry: EntryOutline ) extends ItemSkippedEvent

class EntrySkippedEventFactory(entryAccessor: EntryAccessor[EntryOutline])
  extends ItemSkippedEventFactory[EntrySkippedEvent]{

  override def createAt(nextPosition: Int) = {
    entryAccessor findAt nextPosition map { entry =>
      EntrySkippedEvent(nextPosition, entry)
    }
  }
}

case class EntrySkipStopped(
  override val currentPosition: Int,
  currentEntry: EntryOutline ) extends SkipStoppedEvent

class EntrySkipStoppedFactory(entryAccessor: EntryAccessor[EntryOutline])
  extends SkipStoppedEventFactory[EntrySkipStopped]{

  override def createAt(position: Int) = {
    entryAccessor findAt position map { entry =>
      EntrySkipStopped(position, entry)
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
