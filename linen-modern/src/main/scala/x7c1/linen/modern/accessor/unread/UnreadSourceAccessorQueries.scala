package x7c1.linen.modern.accessor.unread

object UnreadSourceAccessorQueries {

  val sql2 =
    """SELECT
      |  s1.source_id,
      |  s1.channel_id,
      |  s2.start_entry_id
      |FROM channel_source_map AS s1
      |LEFT JOIN source_statuses AS s2
      |  ON (s1.source_id = s2.source_id) AND (s2.account_id = ?)
      |WHERE s1.channel_id = ?
    """.stripMargin

  val sql3 =
    s"""SELECT
      |  t1.source_id AS source_id,
      |  t1.start_entry_id AS start_entry_id,
      |  t2.latest_entry_id AS latest_entry_id
      |FROM ($sql2) AS t1
      |INNER JOIN retrieved_source_marks AS t2 ON t1.source_id = t2.source_id
      |WHERE t2.latest_entry_id > IFNULL(t1.start_entry_id, 0)
    """.stripMargin

  val sql4 =
    s"""SELECT
      |  u1.source_id AS source_id,
      |  u2.title AS title,
      |  u2.description AS description,
      |  u1.latest_entry_id AS latest_entry_id,
      |  u1.start_entry_id AS start_entry_id
      |FROM ($sql3) as u1
      |INNER JOIN sources as u2 ON u1.source_id = u2._id
      """.stripMargin

  val sql5 =
    s"""SELECT
      |  p4.source_id AS source_id,
      |  p4.title AS title,
      |  p4.description AS description,
      |  p4.start_entry_id AS start_entry_id,
      |  p4.latest_entry_id AS latest_entry_id,
      |  p1.account_id,
      |  IFNULL(p1.rating, 100) AS rating
      |FROM ($sql4) AS p4
      |LEFT JOIN source_ratings AS p1
      |  ON (p1.source_id = p4.source_id) AND (p1.account_id = ?)
      """.stripMargin
}
