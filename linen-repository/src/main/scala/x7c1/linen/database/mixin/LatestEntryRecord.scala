package x7c1.linen.database.mixin

import android.database.Cursor
import x7c1.linen.database.struct.{EntryRecord, HasSourceId, retrieved_source_marks}
import x7c1.wheat.macros.database.TypedCursor
import x7c1.wheat.modern.database.Query
import x7c1.wheat.modern.database.selector.RecordReifiable
import x7c1.wheat.modern.database.selector.presets.CanFindRecord

trait LatestEntryRecord extends retrieved_source_marks with EntryRecord

object LatestEntryRecord {
  implicit object reifiable extends RecordReifiable[LatestEntryRecord]{
    override def reify(cursor: Cursor) = TypedCursor[LatestEntryRecord](cursor)
  }
  implicit object findable extends CanFindRecord[HasSourceId, LatestEntryRecord]{
    override def queryAbout[X: HasSourceId](target: X): Query = {
      val sourceId = implicitly[HasSourceId[X]] toId target
      val sql =
        s"""SELECT
           |  t1.latest_entry_id AS latest_entry_id,
           |  t1.latest_entry_created_at AS latest_entry_created_at,
           |  t2.url AS url
           |FROM retrieved_source_marks AS t1
           |INNER JOIN entries AS t2 ON
           |  t2.entry_id = t1.latest_entry_id AND
           |  t2.source_id = ?
           |WHERE t1.source_id = ?
           |""".stripMargin

      Query(sql, Array(sourceId.toString, sourceId.toString))
    }
  }
}
