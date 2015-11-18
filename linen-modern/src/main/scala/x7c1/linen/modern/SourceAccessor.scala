package x7c1.linen.modern

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

trait SourceAccessor {
  def get: Seq[Source]
}

class SourceBuffer extends SourceAccessor {

  private lazy val entriesMapping = mutable.Map[Long, Seq[Long]]()

  private lazy val underlying: ListBuffer[Source] = ListBuffer(createDummies:_*)

  override def get: Seq[Source] = {
    underlying
  }

  def has(sourceId: Long): Boolean = {
    entriesMapping.get(sourceId).exists(_.nonEmpty)
  }
  def firstEntryIdOf(sourceId: Long): Option[Long] = {
    entriesMapping.get(sourceId).flatMap(_.headOption)
  }
  def entryIdBefore(sourceId: Long): Option[Long] = {
    val position = underlying.indexWhere(_.id == sourceId)
    val id = Range(position-1, -1, -1).view.
      map(underlying).
      map(entriesMapping get _.id).
      collectFirst{
        case Some(xs) if xs.nonEmpty => xs.lastOption
      }

    id.flatten
  }

  def updateMapping(sourceId: Long, entryIdList: Seq[Long]) = {
    entriesMapping(sourceId) = entryIdList
  }

  private def createDummies = (1 to 100) map { n =>
    Source(
      id = n,
      url = s"http://example.com/sample-source-$n",
      title = s"sample-title-$n",
      description = s"sample-description-$n" )
  }

}
