package x7c1.linen.modern.init.settings.updater

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView.Adapter
import android.view.ViewGroup
import x7c1.linen.glue.res.layout.{SettingScheduleRow, SettingScheduleRowItem, SettingScheduleTimeRow, SettingScheduleTimeRowItem}
import x7c1.linen.repository.loader.schedule.{LoaderSchedule, LoaderScheduleRow, PresetLoaderSchedule, TimeRange}
import x7c1.wheat.lore.resource.AdapterDelegatee
import x7c1.wheat.modern.decorator.Imports._
import x7c1.wheat.modern.sequence.Sequence

class ScheduleRowAdapter(
  delegatee: AdapterDelegatee[SettingScheduleRow, LoaderScheduleRow],
  providers: ScheduleTimeRowProviders
) extends Adapter[SettingScheduleRow]{

  override def getItemCount = delegatee.count

  override def onBindViewHolder(holder: SettingScheduleRow, position: Int) = {
    delegatee.bindViewHolder(holder, position){
      case (holder: SettingScheduleRowItem, schedule: PresetLoaderSchedule) =>
        holder.name.text = schedule.name
        holder.enabled.checked = schedule.enabled
        holder.timeRanges setLayoutManager new LinearLayoutManager(holder.itemView.context)
        holder.timeRanges setAdapter new ScheduleTimeRowAdapter(
          AdapterDelegatee.create(providers, schedule.startRanges)
        )
      case (holder: SettingScheduleRowItem, schedule: LoaderSchedule) =>
        holder.name.text = schedule.name
        holder.enabled.checked = schedule.enabled
        holder.timeRanges setLayoutManager new LinearLayoutManager(holder.itemView.context)
        holder.timeRanges setAdapter emptyAdapter
    }
  }
  override def onCreateViewHolder(parent: ViewGroup, viewType: Int) = {
    delegatee.createViewHolder(parent, viewType)
  }
  override def getItemViewType(position: Int) = {
    delegatee viewTypeAt position
  }
  private lazy val emptyAdapter = new ScheduleTimeRowAdapter(
    AdapterDelegatee.create(providers, Sequence.from[TimeRange](Seq()))
  )
}

class ScheduleTimeRowAdapter(
  delegatee: AdapterDelegatee[SettingScheduleTimeRow, TimeRange]
) extends Adapter[SettingScheduleTimeRow]{

  override def getItemCount = delegatee.count

  override def onBindViewHolder(holder: SettingScheduleTimeRow, position: Int) = {
    delegatee.bindViewHolder(holder, position){
      case (holder: SettingScheduleTimeRowItem, range) =>
        holder.range.text = range.format
    }
  }
  override def onCreateViewHolder(parent: ViewGroup, viewType: Int) = {
    delegatee.createViewHolder(parent, viewType)
  }
  override def getItemViewType(position: Int) = {
    delegatee viewTypeAt position
  }
}
