package x7c1.linen.modern.init.settings.schedule

import android.view.View
import x7c1.linen.database.struct.LoaderScheduleLike

case class ScheduleSelected(
  scheduleId: Long,
  targetView: View
)
object ScheduleSelected {
  implicit object id extends LoaderScheduleLike[ScheduleSelected]{
    override def toId = _.scheduleId
  }
}
