package x7c1.linen.repository.notification

import android.database.SQLException
import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.database.struct.{HasNotificationKey, NotificationIdRecord}
import x7c1.linen.repository.notification.NotificationIdStore.{IdOutOfBounds, SqlError, StoreError}
import x7c1.wheat.modern.formatter.ThrowableFormatter.format

class NotificationIdStore private (helper: DatabaseHelper){
  def getOrCreate[A: HasNotificationKey](key: A): Either[StoreError, Int] = {
    helper.selectorOf[NotificationIdRecord] findBy key matches {
      case Right(Some(x)) => Right(x.notification_id)
      case Right(None) => create(key)
      case Left(e) => Left(SqlError(e))
    }
  }
  private def create[A: HasNotificationKey](key: A) = {
    val notificationKey = implicitly[HasNotificationKey[A]] toId key
    val either = helper.writable insert notificationKey
    either match {
      case Left(e) => Left(SqlError(e))
      case Right(id) if id < Int.MaxValue => Right(id.toInt)
      case Right(id) => Left(IdOutOfBounds(id))
    }
  }
}

object NotificationIdStore {
  def apply(helper: DatabaseHelper): NotificationIdStore = {
    new NotificationIdStore(helper)
  }

  sealed trait StoreError {
    def detail: String
  }
  case class SqlError(e: SQLException) extends StoreError {
    override def detail: String = format(e.getCause){"[failed]"}
  }
  case class IdOutOfBounds(id: Long) extends StoreError {
    override def detail: String = s"id($id) > ${Int.MaxValue}"
  }
}
