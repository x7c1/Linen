package x7c1.linen.modern.init

import x7c1.linen.modern.action.{Actions, ContainerAction, EntryAreaAction, EntryDetailAreaAction, SourceAreaAction}

trait ActionsInitializer {
  self: ContainerInitializer =>

  def setupActions(): Actions = {
    new Actions(
      new ContainerAction(container),
      new SourceAreaAction(container.sourceArea, accessors.source),
      new EntryAreaAction(
        entryArea = container.entryArea,
        sourceAccessor = accessors.source,
        rawSourceAccessor = accessors.rawSource,
        entryAccessor = accessors.entryOutline
      ),
      new EntryDetailAreaAction(
        container.entryDetailArea,
        accessors.source,
        accessors.rawSource,
        accessors.entryDetail
      )
    )
  }
}
