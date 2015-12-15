package x7c1.linen.modern.action

import x7c1.linen.modern.accessor.{EntryAccessor, SourceAccessor}
import x7c1.linen.modern.display.{EntryDetailSelectedEvent, EntrySelectedEvent, SourceSelectedEvent}
import x7c1.linen.modern.struct.{Entry, EntryDetail, EntryOutline, Source}
import x7c1.wheat.modern.callback.CallbackTask
import x7c1.wheat.modern.observer.{SkipDoneEventFactory, ItemSkippedEventFactory, SkipDoneEvent, ItemSkippedEvent, FocusedEventFactory, ItemFocusedEvent}

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
trait OnSourceSkipDone {
  def onSourceSkipDone(event: SourceSkipDone): CallbackTask[Unit]
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
trait OnEntrySkipDone {
  def onEntrySkipDone(event: EntrySkipDone): CallbackTask[Unit]
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
trait OnEntryDetailSkipDone {
  def onEntryDetailSkipDone(event: EntrySkipDone): CallbackTask[Unit]
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

case class SourceSkipDone(
  override val currentPosition: Int,
  currentSource: Source ) extends SkipDoneEvent

class SourceSkipDoneFactory(sourceAccessor: SourceAccessor)
  extends SkipDoneEventFactory[SourceSkipDone]{

  override def createAt(current: Int) = {
    sourceAccessor findAt current map { source =>
      SourceSkipDone(current, source)
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
  nextEntry: Entry ) extends ItemSkippedEvent

class EntrySkippedEventFactory(entryAccessor: EntryAccessor[Entry])
  extends ItemSkippedEventFactory[EntrySkippedEvent]{

  override def createAt(nextPosition: Int) = {
    entryAccessor findAt nextPosition map { entry =>
      EntrySkippedEvent(nextPosition, entry)
    }
  }
}

case class EntrySkipDone(
  override val currentPosition: Int,
  currentEntry: EntryOutline ) extends SkipDoneEvent

class EntrySkipDoneFactory(entryAccessor: EntryAccessor[EntryOutline])
  extends SkipDoneEventFactory[EntrySkipDone]{

  override def createAt(position: Int) = {
    entryAccessor findAt position map { entry =>
      EntrySkipDone(position, entry)
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
