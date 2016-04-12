package x7c1.linen.modern.init.settings.my

import android.database.SQLException
import x7c1.wheat.modern.formatter.ThrowableFormatter

sealed trait NewChannelError {
  def message: String
  def dump: String
}

sealed trait UserInputError extends NewChannelError

sealed trait UnexpectedError extends NewChannelError

case class EmptyName() extends UserInputError {
  override def message = "name required"
  override def dump = message
}

case class SqlError(e: SQLException) extends UnexpectedError {
  override def message = e.getMessage
  override def dump = ThrowableFormatter.format(e){ "[error]" }
}
