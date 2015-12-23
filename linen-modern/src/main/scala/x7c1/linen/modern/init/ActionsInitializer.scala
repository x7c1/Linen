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
    val panePosition = {
      val length = layout.paneContainer.getChildCount
      val children = 0 to (length - 1) map layout.paneContainer.getChildAt
      new PanePosition(children, displaySize.x)
    }
    val sourceArea = new SourceArea(
      sources = accessors.source,
      recyclerView = layout.sourceList,
      getPosition = () => panePosition of layout.sourceArea
    )
    val entryArea = new EntryArea(
      toolbar = layout.entryToolbar,
      recyclerView = layout.entryList,
      getPosition = () => panePosition of layout.entryArea
    )
    val entryDetailArea = new EntryDetailArea(
      toolbar = layout.entryDetailToolbar,
      recyclerView = layout.entryDetailList,
      getPosition = () => panePosition of layout.entryDetailArea
    )
    new Actions(
      new ContainerAction(
        container = new PaneContainer(layout.paneContainer),
        entryArea,
        entryDetailArea
      ),
      new SourceAreaAction(sourceArea, accessors.source),
      new EntryAreaAction(
        entryArea = entryArea,
        sourceAccessor = accessors.source,
        entryAccessor = accessors.entryOutline
      ),
      new EntryDetailAreaAction(entryDetailArea, accessors.entryDetail)
    )
  }
}
