package x7c1.linen.modern.accessor

import x7c1.linen.modern.struct.Entry

trait OnEntryLoadedListener { self =>

  def onEntryLoaded(e: EntryLoadedEvent): Unit

  def append(listener: OnEntryLoadedListener): OnEntryLoadedListener = {
    new OnEntryLoadedListener {
      override def onEntryLoaded(e: EntryLoadedEvent): Unit = {
        self onEntryLoaded e
        listener onEntryLoaded e
      }
    }
  }
}

object OnEntryLoadedListener {
  def apply(f: EntryLoadedEvent => Unit): OnEntryLoadedListener =
    new OnEntryLoadedListener {
      override def onEntryLoaded(e: EntryLoadedEvent): Unit = f(e)
    }
}

case class EntryLoadedEvent(
  sourceId: Long,
  entries: Seq[Entry] )
