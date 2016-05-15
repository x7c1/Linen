package x7c1.linen.modern.init.settings.updater

import x7c1.linen.database.struct.AccountIdentifiable
import x7c1.wheat.modern.database.selector.presets.DefaultProvidable

sealed trait LoaderScheduleRow

object LoaderScheduleRow {
  implicit object providable extends DefaultProvidable[AccountIdentifiable, LoaderScheduleRow]
}

case class LoaderSchedule(
  name: String,
  enabled: Boolean
) extends LoaderScheduleRow

object LoaderSchedule {
}
