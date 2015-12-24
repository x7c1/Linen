package x7c1.linen.modern.action.observer

import x7c1.linen.modern.action.Actions
import x7c1.linen.modern.display.{PaneFlungEvent, OnPaneFlungListener}

class PaneFlungObserver(actions: Actions) extends OnPaneFlungListener {
  override def onPaneFlung(event: PaneFlungEvent) = {
    actions.container.onPaneFlung(event)
  }
}
