package x7c1.linen.modern.init.settings.updater

import android.support.v7.widget.RecyclerView.Adapter
import android.view.ViewGroup
import x7c1.linen.glue.res.layout.{SettingScheduleRowItem, SettingScheduleRow}
import x7c1.wheat.lore.resource.AdapterDelegatee
import x7c1.wheat.macros.logger.Log
import x7c1.wheat.modern.decorator.Imports._

class ScheduleRowAdapter(
  delegatee: AdapterDelegatee[SettingScheduleRow, LoaderScheduleRow]
) extends Adapter[SettingScheduleRow]{

  override def getItemCount = delegatee.count

  override def onBindViewHolder(holder: SettingScheduleRow, position: Int) = {
    delegatee.bindViewHolder(holder, position){
      case (holder: SettingScheduleRowItem, schedule: LoaderSchedule) =>
        Log info s"$schedule"
        holder.name.text = schedule.name
        holder.enabled.checked = schedule.enabled
    }
  }
  override def onCreateViewHolder(parent: ViewGroup, viewType: Int) = {
    delegatee.createViewHolder(parent, viewType)
  }
  override def getItemViewType(position: Int) = {
    delegatee viewTypeAt position
  }
}
