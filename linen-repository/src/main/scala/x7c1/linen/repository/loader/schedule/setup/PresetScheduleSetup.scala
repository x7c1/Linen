package x7c1.linen.repository.loader.schedule.setup

import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.database.struct.LoaderScheduleKind.AllChannels
import x7c1.linen.database.struct.{AccountIdentifiable, LoaderScheduleKindRecord, LoaderScheduleLike, LoaderScheduleParts, ScheduleKindIdentifiable, ScheduleTimeParts}
import x7c1.linen.repository.date.Date
import x7c1.linen.repository.loader.schedule.PresetLoaderSchedule
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.formatter.ThrowableFormatter.format

class PresetScheduleSetup private (helper: DatabaseHelper){

  def setupFor[A: AccountIdentifiable](account: A) = {
    Log info s"[init] $account"

    def create() = for {
      kind <- findPresetKind.right
      scheduleId <- insertSchedule(account, kind).right
    } yield {
      insertTimes(scheduleId)
      scheduleId
    }
    findPresetSchedule(account) matches {
      case Right(Some(x)) => //nop, already created
      case Right(None) => create() match {
        case Left(error) => Log error s"[create-failed] ${error.message}"
        case Right(scheduleId) => Log info s"schedule(id:$scheduleId) created"
      }
      case Left(e) =>
        Log error format(e){"[failed]"}
    }
  }
  private def findPresetSchedule[A: AccountIdentifiable](account: A) = {
    helper.selectorOf[PresetLoaderSchedule] findBy account
  }
  private def findPresetKind = {
    helper.selectorOf[LoaderScheduleKindRecord] findBy AllChannels matches {
      case Right(Some(kind)) => Right(kind)
      case Right(None) => Left(KindNotFound(AllChannels))
      case Left(e) => Left(SqlError(e))
    }
  }
  private def insertSchedule[A: AccountIdentifiable, B: ScheduleKindIdentifiable](account: A, kind: B) = {
    val either = helper.writable insert LoaderScheduleParts(
      accountId = implicitly[AccountIdentifiable[A]] toId account,
      kindId = implicitly[ScheduleKindIdentifiable[B]] toId kind,
      enabled = true,
      createdAt = Date.current()
    )
    either.left.map(SqlError)
  }
  private def insertTimes[A: LoaderScheduleLike](schedule: A) = {
    val scheduleId = implicitly[LoaderScheduleLike[A]] toId schedule
    val current = Date.current()
    val defaultHours = Seq(5, 13, 21)

    defaultHours map { hour =>
      ScheduleTimeParts(
        scheduleId = scheduleId,
        startHour = hour,
        startMinute = 0,
        createdAt = current
      )
    } map { parts =>
      helper.writable insert parts
    } collect {
      case Left(e) => Log error format(e){"[failed]"}
    }
  }
}

object PresetScheduleSetup {
  def apply(helper: DatabaseHelper): PresetScheduleSetup = new PresetScheduleSetup(helper)
}
