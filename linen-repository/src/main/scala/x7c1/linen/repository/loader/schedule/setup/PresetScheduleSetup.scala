package x7c1.linen.repository.loader.schedule.setup

import x7c1.linen.database.control.DatabaseHelper
import x7c1.linen.database.struct.AccountIdentifiable

class PresetScheduleSetup private (helper: DatabaseHelper){

  def setupFor[A: AccountIdentifiable](account: A) = {
    /*
    def create() = for {
      kind <- findKindOfAllChannels
      scheduleId <- insertSchedule(account, kind)
      _ <- insertTimes(scheduleId)
    } yield {
      scheduleId
    }
    findPresetSchedule(account) match {
      case Right(Some(x)) => //nop, already created
      case Right(None) => create()
      case Left(e) => error!
    }
    */
  }
  /*
  def findPresetSchedule[A: AccountIdentifiable](account: A): Option[...] = {
    helper.selectorOf[PresetLoaderSchedule] findBy account
  }
  def findKindOfAllChannels = {
    helper.selectorOf[LoaderScheduleKind] findBy ScheduleKindLabel.AllChannels matches {
      case Right(Some(kind)) => Right(kind)
      case Right(None) => error!
      case Left(e) => error!
    }
  }
  def insertSchedule[A: AccountIdentifiable, B: ScheduleKindIdentifiable](account: A, kind: B) = {
    helper.writable insert LoaderScheduleParts(
      accountId = implicitly[AccountIdentifiable[A]] toId account,
      kindId = implicitly[ScheduleKindIdentifiable[B]] toId kind,
      enabled = true,
      createdAt = ...,
    )
  }
  def insertTimes[A: LoaderScheduleLike](schedule: A) = {
    val scheduleId = implicitly[LoaderScheduleLike[A]] toId schedule
    val current = ...
    val timePartsList = Seq(
      ScheduleTimeParts(
        scheduleId = scheduleId,
        startHour = 4,
        startMinute = 0,
        createdAt = current
      ),
      ScheduleTimeParts(
        scheduleId = scheduleId,
        startHour = 13,
        startMinute = 0,
        createdAt = current
      ),
    )
    timePartsList map { parts =>
      helper.writable insert parts
    } collect {
      case Left(e) => Log error ...
    }
  }
  */
}

object PresetScheduleSetup {
  def apply(helper: DatabaseHelper): PresetScheduleSetup = new PresetScheduleSetup(helper)
}
