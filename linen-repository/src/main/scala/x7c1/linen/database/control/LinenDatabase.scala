package x7c1.linen.database.control

object LinenDatabase {

  val name: String = "linen-db"
  val version = 1
  def defaults: Seq[String] = {
    val accounts = Seq(
      s"""CREATE TABLE IF NOT EXISTS accounts (
         |_id INTEGER PRIMARY KEY AUTOINCREMENT,
         |nickname TEXT NOT NULL,
         |biography TEXT NOT NULL,
         |created_at INTEGER NOT NULL
         |)""".stripMargin,

      s"""CREATE INDEX accounts_created_at ON accounts (
         |created_at)""".stripMargin
    )
    val accountTags = Seq(
      s"""CREATE TABLE IF NOT EXISTS account_tags (
         |account_tag_id INTEGER PRIMARY KEY AUTOINCREMENT,
         |tag_label TEXT NOT NULL,
         |created_at INTEGER NOT NULL,
         |UNIQUE(tag_label)
         |)""".stripMargin,

      s"""INSERT INTO account_tags (tag_label, created_at)
         |  VALUES ("preset", strftime("%s", CURRENT_TIMESTAMP))""".stripMargin,

      s"""INSERT INTO account_tags (tag_label, created_at)
         |  VALUES ("client", strftime("%s", CURRENT_TIMESTAMP))""".stripMargin
    )
    val accountTagMap = Seq(
      s"""CREATE TABLE IF NOT EXISTS account_tag_map (
         |account_id INTEGER NOT NULL,
         |account_tag_id INTEGER NOT NULL,
         |created_at INTEGER NOT NULL,
         |UNIQUE(account_id, account_tag_id),
         |FOREIGN KEY(account_tag_id) REFERENCES account_tags(account_tag_id) ON DELETE CASCADE,
         |FOREIGN KEY(account_id) REFERENCES accounts(_id) ON DELETE CASCADE
         |)""".stripMargin,

      s"""CREATE INDEX account_tag_map_tag_id ON account_tag_map (
         |account_tag_id)""".stripMargin
    )
    val sources = Seq(
      s"""CREATE TABLE IF NOT EXISTS sources (
         |_id INTEGER PRIMARY KEY AUTOINCREMENT,
         |url TEXT NOT NULL,
         |title TEXT NOT NULL,
         |description TEXT NOT NULL,
         |created_at INTEGER NOT NULL,
         |UNIQUE(url)
         |)""".stripMargin,

      s"""CREATE INDEX sources_created_at ON sources (
         |created_at)""".stripMargin
    )
    val sourceRatings = Seq(
      s"""CREATE TABLE IF NOT EXISTS source_ratings (
         |source_id INTEGER NOT NULL,
         |account_id INTEGER NOT NULL,
         |rating INTEGER NOT NULL,
         |created_at INTEGER NOT NULL,
         |UNIQUE(account_id, source_id),
         |FOREIGN KEY(source_id) REFERENCES sources(_id) ON DELETE CASCADE,
         |FOREIGN KEY(account_id) REFERENCES accounts(_id) ON DELETE CASCADE
         |)""".stripMargin,

      s"""CREATE INDEX source_ratings_id ON source_ratings (
         |account_id, rating, source_id)""".stripMargin
    )
    val entries = Seq(
      s"""CREATE TABLE IF NOT EXISTS entries (
         |_id INTEGER PRIMARY KEY AUTOINCREMENT,
         |source_id INTEGER NOT NULL,
         |url TEXT NOT NULL,
         |title TEXT NOT NULL,
         |content TEXT NOT NULL,
         |created_at INTEGER NOT NULL,
         |UNIQUE(url, source_id),
         |FOREIGN KEY(source_id) REFERENCES sources(_id) ON DELETE CASCADE
         |)""".stripMargin,

      s"""CREATE INDEX entries_source ON entries (
         |source_id, _id)""".stripMargin,

      s"""CREATE INDEX entries_source_created_at ON entries (
         |source_id, created_at)""".stripMargin,

      s"""CREATE INDEX entries_created_at ON entries (
         |created_at)""".stripMargin
    )
    val channels = Seq(
      s"""CREATE TABLE IF NOT EXISTS channels (
         |_id INTEGER PRIMARY KEY AUTOINCREMENT,
         |account_id INTEGER NOT NULL,
         |name TEXT NOT NULL,
         |description TEXT NOT NULL,
         |created_at INTEGER NOT NULL,
         |UNIQUE(account_id, name),
         |FOREIGN KEY(account_id) REFERENCES accounts(_id) ON DELETE CASCADE
         |)""".stripMargin
    )
    val channelSourceMap = Seq(
      s"""CREATE TABLE IF NOT EXISTS channel_source_map (
         |channel_id INTEGER NOT NULL,
         |source_id INTEGER NOT NULL,
         |created_at INTEGER NOT NULL,
         |UNIQUE(channel_id, source_id),
         |FOREIGN KEY(source_id) REFERENCES sources(_id) ON DELETE CASCADE,
         |FOREIGN KEY(channel_id) REFERENCES channels(_id) ON DELETE CASCADE
         |)""".stripMargin
    )
    val channelStatuses = Seq(
      s"""CREATE TABLE IF NOT EXISTS channel_statuses (
         |channel_id INTEGER NOT NULL,
         |account_id INTEGER NOT NULL,
         |subscribed INTEGER NOT NULL,
         |created_at INTEGER NOT NULL,
         |UNIQUE(channel_id, account_id),
         |FOREIGN KEY(account_id) REFERENCES accounts(_id) ON DELETE CASCADE,
         |FOREIGN KEY(channel_id) REFERENCES channels(_id) ON DELETE CASCADE
         |)""".stripMargin,

      s"""CREATE INDEX channel_statuses_account ON channel_statuses (
         |account_id)""".stripMargin
    )
    val retrievedSourceMarks = Seq(
      s"""CREATE TABLE IF NOT EXISTS retrieved_source_marks (
         |source_id INTEGER NOT NULL,
         |latest_entry_id INTEGER NOT NULL,
         |latest_entry_created_at INTEGER NOT NULL,
         |updated_at INTEGER NOT NULL,
         |UNIQUE(source_id),
         |FOREIGN KEY(source_id) REFERENCES sources(_id) ON DELETE CASCADE,
         |FOREIGN KEY(latest_entry_id) REFERENCES entries(_id) ON DELETE CASCADE
         |)""".stripMargin,

      s"""CREATE INDEX retrieved_source_marks_created_at ON retrieved_source_marks (
         |updated_at)""".stripMargin,

      s"""CREATE TRIGGER update_source_marks AFTER INSERT ON entries
         |BEGIN
         |  INSERT OR REPLACE INTO retrieved_source_marks
         |      (source_id, latest_entry_id, latest_entry_created_at, updated_at)
         |    VALUES
         |      (new.source_id, new._id, new.created_at, strftime("%s", CURRENT_TIMESTAMP));
         |END
       """.stripMargin
    )
    val sourceStatuses = Seq(
      s"""CREATE TABLE IF NOT EXISTS source_statuses (
         |source_id INTEGER NOT NULL,
         |start_entry_id INTEGER,
         |start_entry_created_at INTEGER,
         |account_id INTEGER NOT NULL,
         |created_at INTEGER NOT NULL,
         |UNIQUE(source_id, account_id),
         |FOREIGN KEY(source_id) REFERENCES sources(_id) ON DELETE CASCADE,
         |FOREIGN KEY(start_entry_id) REFERENCES entries(_id) ON DELETE CASCADE
         |FOREIGN KEY(account_id) REFERENCES accounts(_id) ON DELETE CASCADE
         |)""".stripMargin
    )
    Seq(
      accounts,
      accountTags,
      accountTagMap,
      sources,
      sourceRatings,
      entries,
      channels,
      channelSourceMap,
      channelStatuses,
      retrievedSourceMarks,
      sourceStatuses
    ).flatten
  }
}
