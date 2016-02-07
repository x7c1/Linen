package x7c1.linen.modern.display.unread

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
