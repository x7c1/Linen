package x7c1.linen.modern.init.unread

import x7c1.linen.modern.action.{Actions, ContainerAction, DrawerAction, OutlineAreaAction, DetailAreaAction, SourceAreaAction}

trait ActionsInitializer {
  self: UnreadItemsDelegatee =>

  def setupActions(): Actions = {
    new Actions(
      new DrawerAction(layout),
      new ContainerAction(container),
      new SourceAreaAction(container.sourceArea, accessors.source),
      new OutlineAreaAction(
        outlineArea = container.outlineArea,
        sourceAccessor = accessors.source,
        rawSourceAccessor = accessors.rawSource,
        entryAccessor = accessors.entryOutline
      ),
      new DetailAreaAction(
        container.detailArea,
        accessors.source,
        accessors.rawSource,
        accessors.entryDetail
      )
    )
  }
}
