package x7c1.linen.repository.entry.unread.selector

import android.database.sqlite.SQLiteDatabase
import x7c1.linen.repository.entry.unread.{ClosableEntryAccessor, EntryRowsBinder, FooterContent, UnreadEntry}
import x7c1.linen.repository.unread.FooterKind

class EntryRowSelector[A <: UnreadEntry](db: SQLiteDatabase){

  def createBinder: EntryRowsBinder[A] = {
    new WithEntriesFooter(EntryRowsBinder())
  }
}

object EntryRowSelector {
  implicit class reify[A <: UnreadEntry](db: SQLiteDatabase) extends EntryRowSelector[A](db)
}

private class WithEntriesFooter[A <: UnreadEntry](
  accessor: EntryRowsBinder[A]) extends EntryRowsBinder[A]{

  override def findAt(position: Int) = {
    if (isLast(position)){
      Some(FooterContent())
    } else {
      accessor.findAt(position)
    }
  }
  override def length = {
    // +1 to append Footer
    accessor.length + 1
  }
  override def findKindAt(position: Int) = {
    if (isLast(position)){
      Some(FooterKind)
    } else {
      accessor findKindAt position
    }
  }
  override def firstEntryPositionOf(sourceId: Long) = {
    accessor firstEntryPositionOf sourceId
  }
  private def isLast(position: Int) = position == accessor.length

  override def lastEntriesTo(position: Int) = {
    accessor lastEntriesTo position
  }
  override def latestEntriesTo(position: Int): Seq[A] = {
    accessor latestEntriesTo position
  }
  override def append(sequence: ClosableEntryAccessor[A]): Unit = {
    accessor.append(sequence)
  }
  override def close(): Unit = {
    accessor.close()
  }
}
