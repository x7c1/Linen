package x7c1.linen.repository.loader.schedule.selector

import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import x7c1.linen.database.struct.HasAccountId
import x7c1.linen.repository.loader.schedule.{LoaderSchedule, PresetLoaderSchedule}
import x7c1.wheat.modern.database.selector.SelectorProvidable.Implicits.SelectorProvidableDatabase
import x7c1.wheat.modern.either.Imports._
import x7c1.wheat.modern.either.OptionEither


class PresetScheduleSelector(protected val db: SQLiteDatabase){
  def findBy[A: HasAccountId](account: A): OptionEither[SQLException, PresetLoaderSchedule] = {
    val either = for {
      schedules <- db.selectorOf[LoaderSchedule].traverseOn(account).right
    } yield schedules.toSeq collectFirst {
      case schedule: PresetLoaderSchedule => schedule
    }
    either.toOptionEither
  }
}

object PresetScheduleSelector {
  implicit def reify: SQLiteDatabase => PresetScheduleSelector = new PresetScheduleSelector(_)
}
