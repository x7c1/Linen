package x7c1.linen.repository.loader.schedule

import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import x7c1.linen.database.mixin.LoaderScheduleWithKind
import x7c1.linen.database.struct.LoaderScheduleKind.AllChannels
import x7c1.linen.database.struct.LoaderScheduleTimeRecord
import x7c1.wheat.modern.database.selector.SelectorProvidable.Implicits._
import x7c1.wheat.modern.database.selector.presets.ClosableSequence
import x7c1.wheat.modern.features.HasShortLength

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object LoaderSchedules {
  implicit object short extends HasShortLength[LoaderScheduleTimeRecord]

  def toTraverseAll(db: SQLiteDatabase): Either[SQLException, ClosableSequence[LoaderSchedule]] = {
    for {
      times <- db.selectorOf[LoaderScheduleTimeRecord].traverseAll().right
      schedules <- db.selectorOf[LoaderScheduleWithKind].traverseAll().right
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
        schedules.findAt(position) collect toSchedule
      }
      override def length: Int = schedules.length

      private lazy val timesMap = {
        val map = mutable.Map[Long, ListBuffer[LoaderScheduleTimeRecord]]()
        times.toSeq.foreach { time =>
          val buffer = map.getOrElseUpdate(time.schedule_id, ListBuffer())
          buffer += time
        }
        map
      }
      private lazy val toSchedule = PartialFunction[LoaderScheduleWithKind, LoaderSchedule]{
        case record if record.schedule_kind_label == AllChannels.label =>
          PresetLoaderSchedule(record, TimeRange fromTimeRecords timesMap(record.schedule_id))
      }
    }

}
