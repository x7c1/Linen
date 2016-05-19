package x7c1.linen.repository.loader.schedule

import android.database.sqlite.SQLiteDatabase
import x7c1.wheat.modern.database.selector.CanProvideSelector
import x7c1.wheat.modern.sequence.Sequence

sealed trait LoaderScheduleRow

object LoaderScheduleRow {
  implicit object providable extends CanProvideSelector[LoaderScheduleRow]{
    override type Selector = ScheduleRowSelector
    override def createFrom(db: SQLiteDatabase) = new ScheduleRowSelector(db)
  }
}

trait LoaderSchedule extends LoaderScheduleRow {
  def scheduleId: Long
  def name: String
  def enabled: Boolean
}

object LoaderSchedule {
  implicit object providable extends CanProvideSelector[LoaderSchedule]{
    override type Selector = ScheduleSelector
    override def createFrom(db: SQLiteDatabase) = new ScheduleSelector(db)
  }
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
