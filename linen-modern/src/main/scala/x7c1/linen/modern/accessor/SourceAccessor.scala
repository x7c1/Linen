package x7c1.linen.modern.accessor

import x7c1.linen.modern.struct.Source

import scala.collection.mutable.ListBuffer

trait SourceAccessor {
  def get(position: Int):Source

  def length: Int

  def takeAfter(sourceId: Long, count: Int): Seq[Source]

  def positionOf(sourceId: Long): Option[Int]
}

class SourceBuffer extends SourceAccessor {

  private lazy val underlying: ListBuffer[Source] = ListBuffer(createDummies:_*)

  override def get(position: Int): Source = {
    underlying(position)
  }
  override def length = underlying.length

  override def takeAfter(sourceId: Long, count: Int): Seq[Source] = {
    val sources = underlying.dropWhile(_.id != sourceId).tail
    sources take count
  }
  override def positionOf(sourceId: Long): Option[Int] = {
    underlying.indexWhere(_.id == sourceId) match {
      case -1 => None
      case position => Some(position)
    }
  }
  private def createDummies = (1 to 300) map { n =>
    Source(
      id = n,
      url = s"http://example.com/sample-source-$n",
      title = s"$n source title",
      description = s"$n source description" )
  }

}
