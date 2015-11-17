package x7c1.linen.modern

import java.util.Date

case class Source(
  id: Long,
  url: String,
  title: String,
  description: String
)

case class Entry(
  sourceId: Long,
  entryId: Long,
  url: String,
  title: String,
  content: String,
  createdAt: LinenDate
)

trait LinenDate {
  def format: String
}

object LinenDate {
  def dummy(): LinenDate = new LinenDateImpl()
}

private class LinenDateImpl extends LinenDate {
  private val underlying = new Date()
  override def format: String = underlying.toString
}
