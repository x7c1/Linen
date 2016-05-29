package x7c1.linen.repository.loader.crawling

import android.database.SQLException
import x7c1.linen.database.struct.HasSourceId

sealed trait SourceInspectorError {
  def message: String
}

case class SourceNotFound[A: HasSourceId](source: A) extends SourceInspectorError {
  private val sourceId = implicitly[HasSourceId[A]] toId source
  override def message: String = s"source(id:$sourceId) not found"
}

case class SqlError(e: SQLException) extends SourceInspectorError {
  override def message: String = e.getMessage
}
