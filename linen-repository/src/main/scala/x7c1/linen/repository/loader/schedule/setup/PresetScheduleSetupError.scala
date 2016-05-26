package x7c1.linen.repository.loader.schedule.setup

import android.database.SQLException
import x7c1.linen.database.struct.LoaderScheduleKind
import x7c1.wheat.modern.formatter.ThrowableFormatter.format

sealed trait PresetScheduleSetupError {
  def message: String
}

case class KindNotFound(kind: LoaderScheduleKind) extends PresetScheduleSetupError {
  override def message = s"schedule_kinds(label:${kind.label}) not found"
}

case class SqlError(e: SQLException) extends PresetScheduleSetupError {
  override def message =
    Seq(
      format(e){"[unexpected]"},
      format(e.getCause){"[cause]"}
    ) mkString "\n"
}
