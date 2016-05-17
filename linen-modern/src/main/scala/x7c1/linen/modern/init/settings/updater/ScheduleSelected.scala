package x7c1.linen.modern.init.settings.updater

import android.view.View

trait ScheduleSelected {
  def targetView: View
}

case class PresetScheduleSelected(
  targetView: View ) extends ScheduleSelected
