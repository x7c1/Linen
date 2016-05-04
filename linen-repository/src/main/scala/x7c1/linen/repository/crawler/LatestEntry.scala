package x7c1.linen.repository.crawler

import x7c1.linen.database.mixin.LatestEntryRecord
import x7c1.linen.database.struct.SourceIdentifiable
import x7c1.linen.repository.date.Date
import x7c1.wheat.modern.database.presets.DefaultProvidable
import x7c1.wheat.modern.database.{CursorConvertible, EntityFindable}


case class LatestEntry(
  entryId: Long,
  entryUrl: String,
  createdAt: Date
)

object LatestEntry {

  implicit object providable extends DefaultProvidable[SourceIdentifiable, LatestEntry]

  implicit object convertible extends CursorConvertible[LatestEntryRecord, LatestEntry]{
    override def fromCursor = {
      case (cursor, position) => cursor.moveToFind(position){
        LatestEntry(
          entryId = cursor.latest_entry_id,
          entryUrl = cursor.url,
          createdAt = cursor.latest_entry_created_at.typed
        )
      }
    }
  }
  implicit object findable extends EntityFindable[SourceIdentifiable, LatestEntryRecord, LatestEntry]
}
