package x7c1.linen.repository.loader.schedule.selector

import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import x7c1.linen.database.mixin.LoaderScheduleWithKind
import x7c1.linen.database.struct.LoaderScheduleKind.AllChannels
import x7c1.linen.database.struct.{HasLoaderScheduleId, LoaderScheduleTimeRecord}
import x7c1.linen.repository.loader.schedule.{LoaderSchedule, PresetLoaderSchedule, ScheduleTime}
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.database.selector.SelectorProvidable.Implicits._
import x7c1.wheat.modern.either.Imports._
import x7c1.wheat.modern.either.OptionEither

class ScheduleSelector(
  protected val db: SQLiteDatabase
) extends ScheduleTraverser with ScheduleFinder {

  protected def createSchedule(
    record: LoaderScheduleWithKind,
    times: Seq[LoaderScheduleTimeRecord]): Option[LoaderSchedule] = {

    record.schedule_kind_label.typed match {
      case AllChannels =>
        Some(PresetLoaderSchedule(record, times map ScheduleTime.fromRecord))
      case unsupported =>
        Log warn s"unsupported kind: ${unsupported.label}"
        None
    }
  }
}

trait ScheduleFinder { self: ScheduleSelector =>
  protected def db: SQLiteDatabase

  def findBy[A: HasLoaderScheduleId](schedule: A): OptionEither[SQLException, LoaderSchedule] = {
    val either = for {
      schedule <- db.selectorOf[LoaderScheduleWithKind].findBy(schedule)
      times <- db.selectorOf[LoaderScheduleTimeRecord].collectFrom(schedule).toOptionEither
    } yield {
      createSchedule(schedule, times)
    }
    either.flatten
  }
}
