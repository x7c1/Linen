package x7c1.linen.modern.init.settings.schedule

import android.support.v7.widget.LinearLayoutManager
import x7c1.linen.glue.res.layout.{SettingScheduleRow, SettingScheduleRowItem, SettingScheduleTimeRow, SettingScheduleTimeRowItem}
import x7c1.linen.repository.loader.schedule.{LoaderSchedule, LoaderScheduleRow, PresetLoaderSchedule, TimeRange}
import x7c1.wheat.lore.resource.AdapterDelegatee
import x7c1.wheat.lore.resource.AdapterDelegatee.BaseAdapter
import x7c1.wheat.modern.decorator.Imports._
import x7c1.wheat.modern.sequence.Sequence

class ScheduleRowAdapter(
  delegatee: AdapterDelegatee[SettingScheduleRow, LoaderScheduleRow],
  providers: ScheduleTimeRowProviders,
  onMenuSelected: ScheduleSelected => Unit,
  onStateChanged: ScheduleStateChanged => Unit
) extends BaseAdapter(delegatee){

  override def onBindViewHolder(holder: SettingScheduleRow, position: Int) = {
    delegatee.bindViewHolder(holder, position){
      case (holder: SettingScheduleRowItem, schedule: PresetLoaderSchedule) =>
        holder.name.text = schedule.name
        holder.enabled.checked = schedule.enabled
        holder.enabled onCheckedChanged { event =>
          onStateChanged(ScheduleStateChanged(schedule.scheduleId, event.isChecked))
        }
        holder.timeRanges setLayoutManager new LinearLayoutManager(holder.itemView.context)
        holder.timeRanges setAdapter new ScheduleTimeRowAdapter(
          AdapterDelegatee.create(providers, Sequence from schedule.startRanges)
        )
        holder.menu onClick { _ =>
          onMenuSelected(ScheduleSelected(schedule.scheduleId, holder.menu))
        }
      case (holder: SettingScheduleRowItem, schedule: LoaderSchedule) =>
        holder.name.text = schedule.name
        holder.enabled.checked = schedule.enabled
        holder.timeRanges setLayoutManager new LinearLayoutManager(holder.itemView.context)
        holder.timeRanges setAdapter emptyAdapter
    }
  }
  private lazy val emptyAdapter = new ScheduleTimeRowAdapter(
    AdapterDelegatee.create(providers, Sequence.from[TimeRange](Seq()))
  )
}

case class ScheduleStateChanged(
  scheduleId: Long,
  enabled: Boolean
)

class ScheduleTimeRowAdapter(
  delegatee: AdapterDelegatee[SettingScheduleTimeRow, TimeRange]
) extends BaseAdapter(delegatee){

  override def onBindViewHolder(holder: SettingScheduleTimeRow, position: Int) = {
    delegatee.bindViewHolder(holder, position){
      case (holder: SettingScheduleTimeRowItem, range) =>
        holder.range.text = range.format
    }
  }
}
