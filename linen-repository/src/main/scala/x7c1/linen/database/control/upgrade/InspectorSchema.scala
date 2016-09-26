package x7c1.linen.database.control.upgrade

object InspectorSchema {
  def init = Seq(
    s"""
      |CREATE TABLE IF NOT EXISTS inspector_actions (
      |   action_id INTEGER PRIMARY KEY AUTOINCREMENT,
      |   action_loading_status INTEGER NOT NULL,
      |   account_id INTEGER NOT NULL,
      |   origin_title TEXT NOT NULL,
      |   origin_url TEXT NOT NULL,
      |   created_at INTEGER NOT NULL,
      |   updated_at INTEGER NOT NULL,
      |   FOREIGN KEY(account_id) REFERENCES accounts(_id) ON DELETE CASCADE
      |)
     """.stripMargin,

    s"""
      |CREATE TABLE IF NOT EXISTS inspector_sources (
      |   action_id INTEGER NOT NULL,
      |   source_loading_status INTEGER NOT NULL,
      |   latent_source_url TEXT NOT NULL,
      |   discovered_source_id INTEGER,
      |   created_at INTEGER NOT NULL,
      |   updated_at INTEGER NOT NULL,
      |   FOREIGN KEY(action_id) REFERENCES inspector_actions(action_id) ON DELETE CASCADE,
      |   FOREIGN KEY(discovered_source_id) REFERENCES sources(_id) ON DELETE SET NULL
      |)
     """.stripMargin
  )
}
