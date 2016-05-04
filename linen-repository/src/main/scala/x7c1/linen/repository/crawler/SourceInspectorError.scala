package x7c1.linen.repository.crawler

import android.database.SQLException
import x7c1.linen.database.struct.SourceIdentifiable

sealed trait SourceInspectorError {
  def message: String
}

case class SourceNotFound[A: SourceIdentifiable](source: A) extends SourceInspectorError {
  private val sourceId = implicitly[SourceIdentifiable[A]] idOf source
  override def message: String = s"source(id:$sourceId) not found"
}

case class SqlError(e: SQLException) extends SourceInspectorError {
  override def message: String = e.getMessage
}
