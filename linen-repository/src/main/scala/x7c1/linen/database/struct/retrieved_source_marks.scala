package x7c1.linen.database.struct

import android.database.Cursor
import x7c1.linen.database.SingleWhere
import x7c1.linen.repository.date.Date
import x7c1.wheat.macros.database.{TypedCursor, TypedFields}

object retrieved_source_marks {
  val table = "retrieved_source_marks"
  implicit object selectable extends SingleWhere[retrieved_source_marks, Long](table){
    override def where(id: Long) = Seq("source_id" -> id.toString)
    override def fromCursor(cursor: Cursor) = {
      val typed = TypedCursor[retrieved_source_marks](cursor)
      typed.freezeAt(0)
    }
  }
}

trait retrieved_source_marks extends TypedFields {
  def source_id: Long
  def latest_entry_id: Long
  def updated_at: Int --> Date
}
