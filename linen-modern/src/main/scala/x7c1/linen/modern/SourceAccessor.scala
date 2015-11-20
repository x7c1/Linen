package x7c1.linen.modern

import scala.collection.mutable.ListBuffer

trait SourceAccessor {
  def get: Seq[Source]

  def takeAfter(sourceId: Long, count: Int): Seq[Source]

  def positionOf(sourceId: Long): Int

  def collectLastFrom[A](sourceId: Long)(f: PartialFunction[Source, A]): Option[A]
}

class SourceBuffer extends SourceAccessor {

  private lazy val underlying: ListBuffer[Source] = ListBuffer(createDummies:_*)

  override def get: Seq[Source] = {
    underlying
  }
  override def takeAfter(sourceId: Long, count: Int): Seq[Source] = {
    val sources = underlying.dropWhile(_.id != sourceId).tail
    sources take count
  }
  override def positionOf(sourceId: Long): Int = {
    underlying.indexWhere(_.id == sourceId)
  }
  override def collectLastFrom[A](sourceId: Long)(f: PartialFunction[Source, A]): Option[A] = {
    val position = underlying.indexWhere(_.id == sourceId)
    Range(position - 1, -1, -1).view map underlying collectFirst f
  }
  private def createDummies = (1 to 100) map { n =>
    Source(
      id = n,
      url = s"http://example.com/sample-source-$n",
      title = s"sample-title-$n",
      description = s"sample-description-$n" )
  }

}
