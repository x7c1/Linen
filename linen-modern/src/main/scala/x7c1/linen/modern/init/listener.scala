package x7c1.linen.modern.init

import android.support.v7.widget.{LinearLayoutManager, RecyclerView}
import x7c1.linen.modern.accessor.{EntryInsertedEvent, EntryLoadedEvent, OnEntryInsertedListener, OnEntryLoadedListener, SourceAccessor, SourcePrefetched, SourceStateBuffer}
import x7c1.wheat.modern.callback.OnFinish

class SourceStateUpdater(
  sourceStateBuffer: SourceStateBuffer) extends OnEntryLoadedListener {

  override def onEntryLoaded(event: EntryLoadedEvent): Unit = {
    sourceStateBuffer.updateState(event.sourceId, SourcePrefetched)
  }
}

class SourceChangedNotifier(
  sourceAccessor: SourceAccessor,
  recyclerView: RecyclerView) extends OnEntryLoadedListener {

  import x7c1.wheat.modern.decorator.Imports._

  override def onEntryLoaded(event: EntryLoadedEvent): Unit = {
    recyclerView runUi { view =>
      sourceAccessor.positionOf(event.sourceId).
        foreach(view.getAdapter.notifyItemChanged)
    }
  }
}

class InsertedEntriesNotifier (
  recyclerView: RecyclerView) extends OnEntryInsertedListener {

  import x7c1.wheat.modern.decorator.Imports._

  private lazy val layoutManager = {
    recyclerView.getLayoutManager.asInstanceOf[LinearLayoutManager]
  }
  override def onInserted(event: EntryInsertedEvent)(done: OnFinish): Unit = {
    recyclerView runUi { view =>
      val current = layoutManager.findFirstCompletelyVisibleItemPosition()
      val base = if (current == event.position) -1 else 0
      view.getAdapter.notifyItemRangeInserted(event.position + base, event.length)
      done.evaluate()
    }
  }
}
