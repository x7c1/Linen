package x7c1.linen.modern.init

import android.graphics.Point
import x7c1.linen.glue.res.layout.MainLayout
import x7c1.linen.modern.action.{EntryDetailAreaAction, EntryAreaAction, SourceAreaAction, ContainerAction, Actions}
import x7c1.linen.modern.display.{PaneContainer, EntryDetailArea, EntryArea, SourceArea}

trait ActionsInitializer {
  def layout: MainLayout
  def accessors: Accessors
  def displaySize: Point

  def setupActions(): Actions = {
    val sourceArea = new SourceArea(
      sources = accessors.source,
      recyclerView = layout.sourceList,
      getPosition = () => 0
    )
    val entryArea = new EntryArea(
      toolbar = layout.entryToolbar,
      recyclerView = layout.entryList,
      getPosition = () => {
        layout.sourceArea.getWidth
      }
    )
    val entryDetailArea = new EntryDetailArea(
      toolbar = layout.entryDetailToolbar,
      recyclerView = layout.entryDetailList,
      getPosition = () => {
        layout.sourceArea.getWidth + layout.entryArea.getWidth
      }
    )
    new Actions(
      new ContainerAction(
        container = new PaneContainer(layout.paneContainer, displaySize.x),
        sourceArea,
        entryArea,
        entryDetailArea
      ),
      new SourceAreaAction(sourceArea, accessors.source),
      new EntryAreaAction(
        entryArea = entryArea,
        sourceAccessor = accessors.source,
        rawSourceAccessor = accessors.rawSource,
        entryAccessor = accessors.entryOutline
      ),
      new EntryDetailAreaAction(
        entryDetailArea,
        accessors.source,
        accessors.rawSource,
        accessors.entryDetail
      )
    )
  }
}
