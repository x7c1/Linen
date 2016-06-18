package x7c1.linen.database.control.upgrade

import x7c1.wheat.calendar.CalendarDate

object ChannelStatusSchema {

  private def timestamp: Int = (CalendarDate.now().toMilliseconds / 1000).toInt

  def rank = Seq(
    "ALTER TABLE channel_statuses ADD COLUMN channel_rank REAL NOT NULL DEFAULT 0",

    s"ALTER TABLE channel_statuses ADD COLUMN updated_at INTEGER NOT NULL DEFAULT $timestamp",

    """CREATE INDEX channel_statuses_rank_order ON
      |  channel_statuses (
      |    account_id,
      |    channel_rank ASC,
      |    updated_at DESC
      |)""".stripMargin
  )
}
