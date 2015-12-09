package x7c1.linen.modern.accessor

import android.database.Cursor
import x7c1.linen.modern.struct.Source

trait SourceAccessor {
  def get(position: Int):Source

  def length: Int

  def takeAfter(sourceId: Long, count: Int): Seq[Source]

  def positionOf(sourceId: Long): Option[Int]
}

class SourceBuffer(cursor: Cursor) extends SourceAccessor {

  private lazy val idIndex = cursor getColumnIndex "_id"
  private lazy val titleIndex = cursor getColumnIndex "title"
  private lazy val descriptionIndex = cursor getColumnIndex "description"

  override def get(position: Int): Source = {
    cursor.moveToPosition(position)

    Source(
      id = cursor.getInt(idIndex),
      url = "dummy",
      title = cursor.getString(titleIndex),
      description = cursor.getString(descriptionIndex)
    )
  }
  override def length = {
    cursor.getCount
  }

  override def takeAfter(sourceId: Long, count: Int): Seq[Source] = {
    ???
    /*
    val sources = underlying.dropWhile(_.id != sourceId).tail
    sources take count
    */
  }
  override def positionOf(sourceId: Long): Option[Int] = {
    (0 to length - 1) find { n =>
      get(n).id == sourceId
    }
    /*
    underlying.indexWhere(_.id == sourceId) match {
      case -1 => None
      case position => Some(position)
    }
    */
  }

}
