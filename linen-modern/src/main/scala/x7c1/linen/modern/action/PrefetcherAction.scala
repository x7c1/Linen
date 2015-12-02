package x7c1.linen.modern.action

import x7c1.linen.modern.accessor.EntryPrefetcher
import x7c1.linen.modern.display.SourceSelectedEvent
import x7c1.wheat.modern.callback.CallbackTask.task

class PrefetcherAction(
  prefetcher: EntryPrefetcher
) extends OnSourceSelected with OnSourceFocused {

  override def onSourceSelected(event: SourceSelectedEvent) = {
    task { prefetcher.triggerBy(event.source.id) }
  }
  override def onSourceFocused(event: SourceFocusedEvent) = {
    task { prefetcher.triggerBy(event.source.id) }
  }
}
