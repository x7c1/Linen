package x7c1.linen.repository.entry.unread.selector

import android.database.sqlite.SQLiteDatabase
import x7c1.linen.repository.entry.unread.{ClosableEntryAccessor, EntryRowsBinder, FooterContent, UnreadEntry}
import x7c1.linen.repository.unread.FooterKind

class EntryRowSelector[A <: UnreadEntry](db: SQLiteDatabase){
  def createBinder: EntryRowsBinder[A] = {
    new WithEntryFooter(EntryRowsBinder())
  }
}

object EntryRowSelector {
  implicit class reify[A <: UnreadEntry](db: SQLiteDatabase) extends EntryRowSelector[A](db)
}

private class WithEntryFooter[A <: UnreadEntry](
  binder: EntryRowsBinder[A]) extends EntryRowsBinder[A]{

  override def findAt(position: Int) = {
    if (isLast(position)){
      Some(FooterContent())
    } else {
      binder.findAt(position)
    }
  }
  override def length = {
    // +1 to append Footer
    binder.length + 1
  }
  override def findKindAt(position: Int) = {
    if (isLast(position)){
      Some(FooterKind)
    } else {
      binder findKindAt position
    }
  }
  override def firstEntryPositionOf(sourceId: Long) = {
    binder firstEntryPositionOf sourceId
  }
  override def lastEntriesTo(position: Int) = {
    binder lastEntriesTo position
  }
  override def latestEntriesTo(position: Int): Seq[A] = {
    binder latestEntriesTo position
  }
  override def append(sequence: ClosableEntryAccessor[A]): Unit = {
    binder.append(sequence)
  }
  override def close(): Unit = {
    binder.close()
  }
  private def isLast(position: Int) = {
    position == binder.length
  }
}
