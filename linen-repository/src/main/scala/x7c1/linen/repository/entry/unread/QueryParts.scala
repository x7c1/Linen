package x7c1.linen.repository.entry.unread

import x7c1.linen.repository.source.unread.UnreadSource

private object QueryParts {

  def limit = 20

  def where = {
    """source_id = ? AND (
      |  (created_at > ?) OR
      |  (created_at = ? AND entry_id > ?)
      |) AND (
      |  (created_at < ?) OR
      |  (created_at = ? AND entry_id <= ?)
      |)
    """.stripMargin
  }
  def toArgs(sources: Seq[UnreadSource]): Array[String] = {
    sources.flatMap(toWhere).toArray
  }
  private def toWhere(x: UnreadSource) = {
    val where = Seq(
      x.accountId,
      x.id,
      x.startEntryCreatedAt getOrElse 0,
      x.startEntryCreatedAt getOrElse 0,
      x.startEntryId getOrElse 0,
      x.latestEntryCreatedAt,
      x.latestEntryCreatedAt,
      x.latestEntryId
    )
    where map (_.toString)
  }
}
