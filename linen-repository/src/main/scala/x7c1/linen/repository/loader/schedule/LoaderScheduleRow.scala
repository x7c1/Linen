package x7c1.linen.repository.loader.schedule

import x7c1.linen.database.struct.AccountIdentifiable
import x7c1.wheat.modern.database.selector.presets.DefaultProvidable
import x7c1.wheat.modern.sequence.Sequence

sealed trait LoaderScheduleRow

object LoaderScheduleRow {
  implicit object providable extends DefaultProvidable[AccountIdentifiable, LoaderScheduleRow]
}

trait LoaderSchedule extends LoaderScheduleRow {
  def scheduleId: Long
  def name: String
  def enabled: Boolean
}

case class PresetLoaderSchedule(
  scheduleId: Long,
  name: String,
  enabled: Boolean,
  startRanges: Sequence[TimeRange]) extends LoaderSchedule

case class ChannelLoaderSchedule(
  scheduleId: Long,
  name: String,
  enabled: Boolean ) extends LoaderSchedule

case class SourceLoaderSchedule(
  scheduleId: Long,
  name: String,
  enabled: Boolean ) extends LoaderSchedule
