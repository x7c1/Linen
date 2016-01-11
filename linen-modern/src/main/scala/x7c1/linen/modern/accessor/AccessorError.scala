package x7c1.linen.modern.accessor

import android.database.SQLException

sealed trait AccessorError {
  def message: String
}

case class NoRecordError(message: String) extends AccessorError

case class SqlError(original: SQLException) extends AccessorError {
  override def message: String = original.getMessage
}
