package x7c1.wheat.modern.decorator.service

sealed trait CommandStartType {
  def value: Int
}

object CommandStartType {
  import android.app.Service.{START_NOT_STICKY, START_REDELIVER_INTENT, START_STICKY}

  case object Sticky extends CommandStartType {
    override def value = START_STICKY
  }
  case object NotSticky extends CommandStartType {
    override def value = START_NOT_STICKY
  }
  case object RedeliverIntent extends CommandStartType {
    override def value = START_REDELIVER_INTENT
  }
}
