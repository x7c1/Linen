package x7c1.linen.modern.action

import x7c1.linen.modern.accessor.{EntryAccessor, UnreadSourceAccessor}
import x7c1.linen.modern.display.unread.{OutlineSelectedEvent, DetailSelectedEvent, SourceSelectedEvent}
import x7c1.linen.modern.struct.{UnreadDetail, UnreadOutline, UnreadSource}
import x7c1.wheat.modern.callback.CallbackTask
import x7c1.wheat.modern.observer.{FocusedEventFactory, ItemFocusedEvent, ItemSkippedEvent, ItemSkippedEventFactory, SkipStoppedEvent, SkipStoppedEventFactory}

class Actions (
  val drawer: DrawerAction,
  val container: ContainerAction,
  val sourceArea: SourceAreaAction,
  val outlineArea: OutlineAreaAction,
  val detailArea: DetailAreaAction
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
trait OnSourceSkipStopped {
  def onSourceSkipStopped(event: SourceSkipStopped): CallbackTask[Unit]
}
trait OnOutlineFocused {
  def onOutlineFocused(event: OutlineFocusedEvent): CallbackTask[Unit]
}
trait OnOutlineSelected {
  def onOutlineSelected(event: OutlineSelectedEvent): CallbackTask[Unit]
}
trait OnOutlineSkipped {
  def onOutlineSkipped(event: EntrySkippedEvent): CallbackTask[Unit]
}
trait OnOutlineSkipStopped {
  def onOutlineSkipStopped(event: EntrySkipStopped): CallbackTask[Unit]
}
trait OnDetailFocused {
  def onDetailFocused(event: DetailFocusedEvent): CallbackTask[Unit]
}
trait OnDetailSelected {
  def onDetailSelected(event: DetailSelectedEvent): CallbackTask[Unit]
}
trait OnDetailSkipped {
  def onDetailSkipped(event: EntrySkippedEvent): CallbackTask[Unit]
}
trait OnDetailSkipStopped {
  def onDetailSkipStopped(event: EntrySkipStopped): CallbackTask[Unit]
}

case class SourceFocusedEvent(
  override val position: Int,
  source: UnreadSource) extends ItemFocusedEvent

class SourceFocusedEventFactory(sourceAccessor: UnreadSourceAccessor)
  extends FocusedEventFactory[SourceFocusedEvent] {

  override def createAt(position: Int) = {
    sourceAccessor findAt position map { source =>
      SourceFocusedEvent(position, source)
    }
  }
}

case class SourceSkippedEvent(
  override val nextPosition: Int,
  nextSource: UnreadSource ) extends ItemSkippedEvent

class SourceSkippedEventFactory(sourceAccessor: UnreadSourceAccessor)
  extends ItemSkippedEventFactory[SourceSkippedEvent]{

  override def createAt(next: Int) = {
    sourceAccessor findAt next map { source =>
      SourceSkippedEvent(next, source)
    }
  }
}

case class SourceSkipStopped(
  override val currentPosition: Int,
  currentSource: UnreadSource ) extends SkipStoppedEvent

class SourceSkipStoppedFactory(sourceAccessor: UnreadSourceAccessor)
  extends SkipStoppedEventFactory[SourceSkipStopped]{

  override def createAt(current: Int) = {
    sourceAccessor findAt current map { source =>
      SourceSkipStopped(current, source)
    }
  }
}

class OutlineFocusedEvent(
  override val position: Int,
  entry: UnreadOutline) extends ItemFocusedEvent {
  val sourceId: Long = entry.sourceId
}

class OutlineFocusedEventFactory(entryAccessor: EntryAccessor[UnreadOutline])
  extends FocusedEventFactory[OutlineFocusedEvent] {

  override def createAt(position: Int) = {
    entryAccessor findAt position map { entry =>
      new OutlineFocusedEvent(position, entry)
    }
  }
}

class EntrySkippedEvent(
  override val nextPosition: Int,
  nextEntry: UnreadOutline ) extends ItemSkippedEvent {
  val nextSourceId: Long = nextEntry.sourceId
}

class EntrySkippedEventFactory(entryAccessor: EntryAccessor[UnreadOutline])
  extends ItemSkippedEventFactory[EntrySkippedEvent]{

  override def createAt(nextPosition: Int) = {
    entryAccessor findAt nextPosition map { entry =>
      new EntrySkippedEvent(nextPosition, entry)
    }
  }
}

class EntrySkipStopped(
  override val currentPosition: Int,
  currentEntry: UnreadOutline ) extends SkipStoppedEvent {
  val currentSourceId: Long = currentEntry.sourceId
}

class EntrySkipStoppedFactory(entryAccessor: EntryAccessor[UnreadOutline])
  extends SkipStoppedEventFactory[EntrySkipStopped]{

  override def createAt(position: Int) = {
    entryAccessor findAt position map { entry =>
      new EntrySkipStopped(position, entry)
    }
  }
}

class DetailFocusedEvent(
  override val position: Int,
  entry: UnreadDetail ) extends ItemFocusedEvent {
  val sourceId: Long = entry.sourceId
}

class DetailFocusedEventFactory(entryAccessor: EntryAccessor[UnreadDetail])
  extends FocusedEventFactory[DetailFocusedEvent]{

  override def createAt(position: Int) = {
    entryAccessor findAt position map { entry =>
      new DetailFocusedEvent(position, entry)
    }
  }
}
