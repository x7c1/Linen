package x7c1.linen.modern.display.unread

import android.view.View
import android.view.View.OnClickListener
import x7c1.linen.glue.res.layout.{MenuRowLabel, MenuRowTitle}
import x7c1.linen.modern.accessor.unread.ChannelSelectable
import x7c1.wheat.ancient.resource.ViewHolderProvider
import x7c1.wheat.modern.menu.SingleMenuText

sealed trait MenuItemKind {
  def body: String
}

object MenuItemKind {
  case class UnreadChannelMenu(channelId: Long, body: String) extends MenuItemKind
  case class NoChannel(body: String) extends MenuItemKind
  case class MyChannels(body: String) extends MenuItemKind
  case class PresetChannels(body: String) extends MenuItemKind
  case class ChannelOrder(body: String) extends MenuItemKind
  case class UpdaterSchedule(body: String) extends MenuItemKind

  case class DevCreateDummies(body: String) extends MenuItemKind

  object UnreadChannelMenu {
    implicit object selectable extends ChannelSelectable[UnreadChannelMenu] {
      override def channelIdOf = _.channelId
      override def nameOf = _.body
    }
  }
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
