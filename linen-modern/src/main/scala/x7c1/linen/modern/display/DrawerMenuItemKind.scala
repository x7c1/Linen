package x7c1.linen.modern.display

sealed trait DrawerMenuItemKind {
  def body: String
}

object DrawerMenuItemKind {
  case class NoList(body: String) extends DrawerMenuItemKind
  case class Lists(body: String) extends DrawerMenuItemKind
  case class Sources(body: String) extends DrawerMenuItemKind
  case class CrawlerSchedule(body: String) extends DrawerMenuItemKind
}
