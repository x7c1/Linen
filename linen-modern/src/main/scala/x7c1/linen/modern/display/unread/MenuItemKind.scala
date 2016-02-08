package x7c1.linen.modern.display.unread

import android.view.View
import android.view.View.OnClickListener
import x7c1.linen.glue.res.layout.{MenuRowLabel, MenuRowTitle}
import x7c1.wheat.ancient.resource.ViewHolderProvider
import x7c1.wheat.modern.menu.SingleMenuText

sealed trait MenuItemKind {
  def body: String
}

object MenuItemKind {
  case class NoChannel(body: String) extends MenuItemKind
  case class Channels(body: String) extends MenuItemKind
  case class ChannelSources(body: String) extends MenuItemKind
  case class UpdaterSchedule(body: String) extends MenuItemKind

  case class DevCreateDummies(body: String) extends MenuItemKind
  case class DevShowRecords(body: String) extends MenuItemKind
}

class DrawerMenuTitleFactory(provider: ViewHolderProvider[MenuRowTitle]){
  def of(text: String): SingleMenuText[MenuRowTitle] = {
    new SingleMenuText(text, provider)
  }
}

class DrawerMenuLabel(
  text: String,
  provider: ViewHolderProvider[MenuRowLabel],
  val onClick: OnClickListener ) extends SingleMenuText(text, provider)

class DrawerMenuLabelFactory(
  provider: ViewHolderProvider[MenuRowLabel], listener: OnMenuItemClickListener){

  def of(kind: MenuItemKind): DrawerMenuLabel =
    new DrawerMenuLabel(kind.body, provider, new OnClickListener {
      override def onClick(v: View): Unit = listener onClick kind
    })
}

trait OnMenuItemClickListener {
  def onClick(kind: MenuItemKind): Unit
}
