package x7c1.linen.database.control.upgrade

object NotificationIdSchema {
  def init = Seq(
    s"""CREATE TABLE IF NOT EXISTS notification_ids (
       |  notification_id INTEGER PRIMARY KEY AUTOINCREMENT,
       |  notification_content_kind TEXT NOT NULL,
       |  notification_content_key TEXT NOT NULL,
       |  created_at INTEGER NOT NULL,
       |  UNIQUE(notification_content_kind, notification_content_key)
       |)""".stripMargin,

    s"""CREATE INDEX notification_ids_key ON notification_ids (
       |  notification_content_kind, notification_content_key
       |)""".stripMargin
  )
}
