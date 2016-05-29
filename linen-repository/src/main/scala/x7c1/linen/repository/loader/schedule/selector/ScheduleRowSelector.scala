package x7c1.linen.repository.loader.schedule.selector

import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import x7c1.linen.database.struct.HasAccountId
import x7c1.linen.repository.loader.schedule.{LoaderSchedule, LoaderScheduleRow}
import x7c1.wheat.modern.database.selector.SelectorProvidable.Implicits._
import x7c1.wheat.modern.sequence.Sequence

class ScheduleRowSelector(db: SQLiteDatabase){
  def traverseOn[A: HasAccountId](account: A): Either[SQLException, Sequence[LoaderScheduleRow]] = {
    db.selectorOf[LoaderSchedule].traverseOn(account)
  }
}
