package x7c1.linen.repository.unread

import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.database.struct.{AccountRecord, EntryRecord, SourceRecord, SourceStatusAsStarted}
import x7c1.linen.repository.entry.unread.{UnreadOutline, EntryAccessor, UnreadEntry}
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.formatter.ThrowableFormatter

class BrowsedEntriesMarker (
  helper: DatabaseHelper,
  outlineAccessor: EntryAccessor[UnreadOutline]){

  private var outlinePosition: Option[Int] = None

  def touchOutlinePosition(position: Int): Unit = {
    outlinePosition match {
      case Some(current) if current >= position =>
      case _ => outlinePosition = Some(position)
    }
  }

  /*
  private val map = collection.mutable.Map[Long, UnreadEntry]()
  private def noteAsRead[A <: UnreadEntry](entry: A) = {
    val shouldUpdate = map.get(entry.sourceId) match {
      case Some(current) if entry olderThan current => true
      case None => true
      case _ => false
    }
    if (shouldUpdate){
      map.update(entry.sourceId, entry)
    }
  }
  */

  def markAsRead(): Unit = {
    Log info s"[init]"

    val outlines = outlinePosition map
      outlineAccessor.latestEntriesTo getOrElse Seq()

    val map = browsedMap(outlines)
    Log info s"${map.size}, $map"

    map foreach {
      case (sourceId, entry) =>
        Log info s"account:${entry.accountId}, ${entry.entryId}, ${entry.sourceId}"

        val x1 = helper.selectorOf[EntryRecord] findBy entry
        val x2 = helper.readable.find[AccountRecord] by entry.accountId
        val x3 = helper.selectorOf[SourceRecord] findBy entry

        Log info s"$x1"
        Log info s"$x2"
        Log info s"$x3"

        helper.writable replace SourceStatusAsStarted(
          startEntryId = entry.entryId,
          startEntryCreatedAt = entry.createdAt.timestamp,
          sourceId = entry.sourceId,
          accountId = entry.accountId
        ) match {
          case Left(e) => Log error ThrowableFormatter.format(e){"[failed]"}
          case Right(b) => Log info s"[done] id:$b"
        }
        Log info s"$sourceId, ${entry.shortTitle}"
    }
  }
  private def browsedMap[A <: UnreadEntry](entries: Seq[A]) = {
    val pairs = entries.map { entry => entry.sourceId -> entry }
    pairs.toMap
  }
}

object BrowsedEntriesMarker {
  def apply(helper: DatabaseHelper, accessors: Accessors): BrowsedEntriesMarker = {
    new BrowsedEntriesMarker(helper, outlineAccessor = accessors.entryOutline)
  }
}