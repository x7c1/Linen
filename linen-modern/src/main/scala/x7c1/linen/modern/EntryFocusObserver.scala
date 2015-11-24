package x7c1.linen.modern

import x7c1.wheat.modern.callback.OnFinish

class EntryFocusObserver(
  sourceAccessor: SourceAccessor,
  entryAccessor: EntryAccessor,
  sourceArea: SourceArea) extends OnItemFocusedListener {

  override def onItemFocused(event: ItemFocusedEvent): Unit = {
    val entry = entryAccessor.get(event.position)
    val sourceId = sourceAccessor.positionOf(entry.sourceId)

    sourceArea.scrollTo(sourceId)(OnFinish({}))
  }
}
