package x7c1.linen.modern.init.updater

import android.database.SQLException

sealed trait SourceInspectorError {
  def message: String
}

case class SourceNotFound(sourceId: Long) extends SourceInspectorError {
  override def message: String = s"source(id:$sourceId) not found"
}

case class SqlError(e: SQLException) extends SourceInspectorError {
  override def message: String = e.getMessage
}
