package x7c1.linen.modern.display

sealed trait DrawerMenuItemKind {
  def body: String
}

object DrawerMenuItemKind {
  case class NoChannel(body: String) extends DrawerMenuItemKind
  case class Channels(body: String) extends DrawerMenuItemKind
  case class ChannelSources(body: String) extends DrawerMenuItemKind
  case class UpdaterSchedule(body: String) extends DrawerMenuItemKind

  case class DevCreateDummies(body: String) extends DrawerMenuItemKind
  case class DevShowRecords(body: String) extends DrawerMenuItemKind
}
