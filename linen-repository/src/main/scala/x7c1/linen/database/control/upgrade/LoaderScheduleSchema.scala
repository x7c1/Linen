package x7c1.linen.database.control.upgrade

object LoaderScheduleSchema {
  def init = Seq(
    s"""
      |CREATE TABLE IF NOT EXISTS loader_schedule_kinds (
      |schedule_kind_id INTEGER PRIMARY KEY AUTOINCREMENT,
      |schedule_kind_label TEXT NOT NULL,
      |created_at INTEGER NOT NULL,
      |UNIQUE(schedule_kind_label)
      |)""".stripMargin,

    s"""
      |INSERT INTO loader_schedule_kinds (schedule_kind_label, created_at)
      |  VALUES ("all_channels", strftime("%s", CURRENT_TIMESTAMP))""".stripMargin,

    s"""
      |CREATE TABLE IF NOT EXISTS loader_schedules (
      |schedule_id INTEGER PRIMARY KEY AUTOINCREMENT,
      |account_id INTEGER NOT NULL,
      |schedule_kind_id INTEGER NOT NULL,
      |enabled INTEGER NOT NULL,
      |created_at INTEGER NOT NULL,
      |FOREIGN KEY(schedule_kind_id) REFERENCES loader_schedule_kinds(schedule_kind_id) ON DELETE CASCADE,
      |FOREIGN KEY(account_id) REFERENCES accounts(_id) ON DELETE CASCADE
      |)""".stripMargin,

    s"""
      |CREATE TABLE IF NOT EXISTS loader_schedule_times (
      |schedule_time_id INTEGER PRIMARY KEY AUTOINCREMENT,
      |schedule_id INTEGER NOT NULL,
      |start_hour INTEGER NOT NULL,
      |start_minute INTEGER NOT NULL,
      |created_at INTEGER NOT NULL,
      |FOREIGN KEY(schedule_id) REFERENCES loader_schedules(schedule_id) ON DELETE CASCADE
      |)""".stripMargin
  )
}
