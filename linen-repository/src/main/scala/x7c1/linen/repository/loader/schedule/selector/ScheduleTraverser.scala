package x7c1.linen.repository.loader.schedule.selector

import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import x7c1.linen.database.mixin.LoaderScheduleWithKind
import x7c1.linen.database.struct.{AccountIdentifiable, LoaderScheduleTimeRecord}
import x7c1.linen.repository.loader.schedule.LoaderSchedule
import x7c1.wheat.modern.database.selector.SelectorProvidable.Implicits._
import x7c1.wheat.modern.database.selector.presets.ClosableSequence

trait ScheduleTraverser { self: ScheduleSelector =>
  protected def db: SQLiteDatabase

  type Traversed = Either[SQLException, ClosableSequence[LoaderSchedule]]

  def traverseAll(): Traversed = {
    for {
      times <- db.selectorOf[LoaderScheduleTimeRecord].traverseAll().right
      schedules <- db.selectorOf[LoaderScheduleWithKind].traverseAll().right
    } yield {
      createFrom(times, schedules)
    }
  }
  def traverseOn[A: AccountIdentifiable](account: A): Traversed = {
    for {
      times <- db.selectorOf[LoaderScheduleTimeRecord].traverseOn(account).right
      schedules <- db.selectorOf[LoaderScheduleWithKind].traverseOn(account).right
    } yield {
      createFrom(times, schedules)
    }
  }
  private def createFrom(
    times: ClosableSequence[LoaderScheduleTimeRecord],
    schedules: ClosableSequence[LoaderScheduleWithKind]): ClosableSequence[LoaderSchedule] =

    new ClosableSequence[LoaderSchedule] {
      override def closeCursor() = {
        times.closeCursor()
        schedules.closeCursor()
      }
      override def findAt(position: Int) = {
        schedules.findAt(position) flatMap { record =>
          createSchedule(record, timesMap(record.schedule_id))
        }
      }
      override def length: Int = schedules.length

      private val timesMap = times.groupByScheduleId
    }

}
