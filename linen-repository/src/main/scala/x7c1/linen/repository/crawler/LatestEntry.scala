package x7c1.linen.repository.crawler

import android.database.Cursor
import x7c1.linen.database.struct.{EntryRecord, retrieved_source_marks}
import x7c1.linen.repository.date.Date
import x7c1.wheat.macros.database.TypedCursor
import x7c1.wheat.modern.database.{Query, SingleSelectable}


case class LatestEntry(
  entryId: Long,
  entryUrl: String,
  createdAt: Date
)

object LatestEntry {
  implicit object selectable extends SingleSelectable[LatestEntry, Long]{
    override def query(sourceId: Long) = {
      val sql =
        s"""SELECT
           |  t1.latest_entry_id AS latest_entry_id,
           |  t1.latest_entry_created_at AS latest_entry_created_at,
           |  t2.url AS url
           |FROM retrieved_source_marks AS t1
           |INNER JOIN entries AS t2 ON
           |  t2._id = t1.latest_entry_id AND
           |  t2.source_id = ?
           |WHERE t1.source_id = ?
           |""".stripMargin

      new Query(sql, Array(sourceId.toString, sourceId.toString))
    }
    override def fromCursor(cursor: Cursor) = {
      TypedCursor[LatestEntryRecord](cursor) moveToHead reify
    }
  }
  private def reify(record: LatestEntryRecord) = {
    LatestEntry(
      entryId = record.latest_entry_id,
      entryUrl = record.url,
      createdAt = record.latest_entry_created_at.typed
    )
  }
}

trait LatestEntryRecord extends retrieved_source_marks with EntryRecord