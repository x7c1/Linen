package x7c1.linen.modern.init.unread

import x7c1.linen.repository.entry.unread.UnreadEntry
import x7c1.wheat.macros.logger.Log

class BrowsedEntriesMarker (accessors: Accessors){

  private var outlinePosition: Option[Int] = None

  def noteOutlinePosition(position: Int): Unit = {
    outlinePosition = Some(position)
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
      accessors.entryOutline.lastEntriesTo getOrElse Seq()

    val map = browsedMap(outlines)
    Log info s"${map.size}, $map"
    map foreach {
      case (sourceId, entry) =>
        Log info s"$sourceId, ${entry.shortTitle}"
    }
  }
  private def browsedMap[A <: UnreadEntry](entries: Seq[A]) = {
    val pairs = entries.map { entry => entry.sourceId -> entry }
    pairs.toMap
  }
}
