package x7c1.linen.repository.account.setup

import x7c1.linen.database.struct.AccountTagLabel
import x7c1.linen.repository.error.ApplicationError
import x7c1.wheat.modern.formatter.ThrowableFormatter

sealed trait AccountSetupError extends ApplicationError

case class AccountTagNotFound(tagLabel: AccountTagLabel) extends AccountSetupError {
  override def detail: String = s"account tag [${tagLabel.text}] not found"
}

case class UnexpectedException(cause: Exception) extends AccountSetupError {
  override def detail: String = ThrowableFormatter.format(cause){"[error]"}
}
