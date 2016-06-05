package x7c1.linen.repository.source.unread.selector

import android.database.sqlite.SQLiteDatabase
import x7c1.linen.repository.source.unread.{ClosableSourceAccessor, SourceFooterContent}

class SourceRowSelector(db: SQLiteDatabase){
  def createHolder: SourceRowHolder = {
    new WithSourceFooter(new SourceRowHolderImpl)
  }
}

object SourceRowSelector{
  implicit class reify(db: SQLiteDatabase) extends SourceRowSelector(db)
}

trait SourceRowHolder extends ClosableSourceAccessor {
  def addLength(size: Int): Unit
  def updateSequence(sequence: ClosableSourceAccessor): Unit
}

class SourceRowHolderImpl extends SourceRowHolder {

  private var underlying: Option[ClosableSourceAccessor] = None

  private var currentLength: Int = 0

  def addLength(size: Int): Unit = {
    currentLength += size
  }
  def updateSequence(sequence: ClosableSourceAccessor): Unit = {
    underlying = Some(sequence)
  }
  override def findAt(position: Int) = {
    underlying.flatMap(_ findAt position)
  }
  override def positionOf(sourceId: Long): Option[Int] = {
    underlying.flatMap(_ positionOf sourceId)
  }
  override def length: Int = {
    currentLength
  }
  override def close(): Unit = synchronized {
    underlying foreach {_.close()}
    underlying = None
    currentLength = 0
  }
}

private class WithSourceFooter(holder: SourceRowHolder) extends SourceRowHolder {

  override def findAt(position: Int) = {
    if (isLast(position)){
      Some(SourceFooterContent())
    } else {
      holder findAt position
    }
  }
  override def positionOf(sourceId: Long) = {
    holder positionOf sourceId
  }
  override def length: Int = {
    // +1 to append Footer
    holder.length + 1
  }
  override def close(): Unit = {
    holder.close()
  }
  override def addLength(size: Int): Unit = {
    holder.addLength(size)
  }
  override def updateSequence(sequence: ClosableSourceAccessor): Unit = {
    holder.updateSequence(sequence)
  }
  private def isLast(position: Int) = {
    position == holder.length
  }
}
